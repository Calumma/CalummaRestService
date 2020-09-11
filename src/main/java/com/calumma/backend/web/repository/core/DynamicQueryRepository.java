package com.calumma.backend.web.repository.core;

import com.calumma.backend.model.entity.CalummaEntity;
import com.calumma.backend.web.repository.core.aggregation.CoreEntityGrouper;
import com.calumma.backend.web.repository.core.aggregation.CoreEntityHaving;
import com.calumma.backend.web.repository.core.projections.CoreEntityProjectionBuilder;
import com.calumma.backend.web.repository.core.symbol.ProjectionField;
import com.calumma.backend.web.repository.core.symbol.ResponseParser;
import com.calumma.backend.model.request.ParsedRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.criteria.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@SuppressWarnings({"unchecked", "rawtypes"})
public class DynamicQueryRepository<Entity extends CalummaEntity> {

    private EntityManager entityManager;
    private final CriteriaBuilder criteriaBuilder;


    @Autowired
    public DynamicQueryRepository(EntityManager entityManager) {
        criteriaBuilder = entityManager.getCriteriaBuilder();
        this.entityManager = entityManager;
    }

    public Page queryBy(Class<Entity> entityClass, Specification filters, ParsedRequest parsedRequest)
            throws NoSuchFieldException, ParseException, IllegalAccessException {

        CriteriaQuery query = criteriaBuilder.createQuery();
        Root<?> root = query.from(entityClass);
        Page response = null;

        CoreEntityGrouper coreEntityGrouper = new CoreEntityGrouper(root, entityClass);
        CoreEntityHaving coreEntityHaving = new CoreEntityHaving(root, entityClass, criteriaBuilder);
        CoreEntityProjectionBuilder projectionBuilder = new CoreEntityProjectionBuilder(root, entityClass, criteriaBuilder);

        query = applyRestrictionConditions(root, filters, query);
        query = applySelectionFields(projectionBuilder, entityClass, root, parsedRequest.getProjections(), query);
        query.orderBy(QueryUtils.toOrders(parsedRequest.getPageable().getSort(), root, criteriaBuilder));
        query = coreEntityGrouper.getGroupByClause(parsedRequest.getGroupBy(), query);
        query = coreEntityHaving.getHavingClause(parsedRequest.getAggregationFilters(), query);

        List<Object> result = entityManager.createQuery(query)
                                           .setMaxResults(parsedRequest.getPageable().getPageSize())
                                           .setFirstResult((int) parsedRequest.getPageable().getOffset())
                                           .getResultList();

        ResponseParser parser = new ResponseParser(parsedRequest.getNotEagerProjections(), result);
        List<Map<String, Object>> responseMap = parser.getFormattedResponseDictionary().getResponse();
        parser.setResponse(queryEagerAttributes(entityClass, projectionBuilder.getEagerSelections(), responseMap));
        response = parser.getFormattedResponse(parsedRequest.getPageable(), countQuery(entityClass, filters));

        return response;
    }

    public List<Map<String, Object>> queryEagerAttributes(Class<Entity> entityClass, List<ProjectionField> eagerQueries, List<Map<String, Object>> response)
            throws NoSuchFieldException, IllegalAccessException {

        List<Long> rootIds = response.stream().map(x -> (Long) x.get("id")).collect(Collectors.toList());

        for (ProjectionField eagerQueryProjections : eagerQueries) {
            CriteriaQuery<Object> eagerQuery = criteriaBuilder.createQuery();
            Root<?> root = eagerQuery.from(entityClass);
            root.get("id").in(rootIds.stream().map(x -> (Long) x).collect(Collectors.toList()));

            CoreEntityProjectionBuilder projectionBuilder = new CoreEntityProjectionBuilder(root, entityClass, criteriaBuilder);
            eagerQuery = applySelectionFields(projectionBuilder, entityClass, root, getEagerQueryProjections(eagerQueryProjections.getNameField()), eagerQuery);

            List<Object> result = entityManager.createQuery(eagerQuery).getResultList();

            ResponseParser parser = new ResponseParser(getEagerQueryProjections(eagerQueryProjections.getNameField()), result);
            List<Map<String, Object>> responseMap = parser.getFormattedResponseDictionary().getResponse();

            for (Map<String, Object> globalResponse: response) {
                Long id = (Long) globalResponse.get("id");
                globalResponse.put(eagerQueryProjections.getAlias(), new ArrayList<Object>());
                for(Map<String, Object> localResponse: responseMap){
                    Long localId = (Long) localResponse.get("id");
                    if(localId != null && localId.equals(id)){
                        List local = (List) globalResponse.get(eagerQueryProjections.getAlias());
                        localResponse.remove("id");
                        local.add(localResponse);
                    }
                }
            }
        }

        return response;
    }

    private CriteriaQuery applySelectionFields(CoreEntityProjectionBuilder projectionBuilder, Class<Entity> entityClass,
                                               Root root, List<ProjectionField> projections,
                CriteriaQuery query) throws NoSuchFieldException, IllegalAccessException {
     if(projections != null && projections.size() > 0) {
            projections.forEach(projectionBuilder::with);

            List<Selection> selects = projectionBuilder.build();

            if (!selects.isEmpty())
                query.multiselect(selects.toArray(new Selection[]{}));
            else
                query.multiselect(root);
        }else {
            query.multiselect(root);
        }

        return query;
    }

    private CriteriaQuery applyRestrictionConditions(Root root, Specification filters, CriteriaQuery query) {
        if (filters != null) {
            Predicate[] predicates = {filters.toPredicate(root, query, criteriaBuilder)};
            query.where(predicates);
        }

        return query;
    }

    private long countQuery(Class<Entity> entityClass, Specification filters) throws NoSuchFieldException {

        CriteriaQuery<Long> query = criteriaBuilder.createQuery(Long.class);
        Root<?> root = query.from(entityClass);

        query.select(criteriaBuilder.count(root));
        query = applyRestrictionConditions(root, filters, query);
        Object response = entityManager.createQuery(query).getSingleResult();

        return (long) response;
    }

    private List<ProjectionField> getEagerQueryProjections(String eager){
        String[] projections  = (eager.replaceAll(";", ",") + ",id").split(",");
        List<ProjectionField> result = new ArrayList<>();
        Arrays.stream(projections).forEach(x -> {
            if(x.contains(".")){
                String[] fields = x.split("\\.");
                String alias = fields[fields.length - 1];
                result.add(new ProjectionField(x, alias));
            }else{
                result.add(new ProjectionField(x, x));
            }
        });

        return result;
    }
}
