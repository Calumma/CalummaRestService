package com.calumma.backend.web.repository.core.aggregation;

import com.calumma.backend.model.entity.CalummaEntity;
import com.calumma.backend.web.repository.core.symbol.*;

import javax.persistence.criteria.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class CoreEntityHaving<Entity extends CalummaEntity> {

    private Root<Entity> root;
    private Class entityType;
    private final CriteriaBuilder criteriaBuilder;

    public CoreEntityHaving(Root<Entity> root, Class entityType, CriteriaBuilder criteriaBuilder) {
        this.root = root;
        this.entityType = entityType;
        this.criteriaBuilder = criteriaBuilder;
    }

    private List<Predicate> toSelection(List<SearchCriteria> aggregationFilters)
            throws NoSuchFieldException, ParseException {
        List<Predicate> groupExpressions = new ArrayList<>();

        for(SearchCriteria searchCriteria: aggregationFilters){
            ParsedField field = FieldParser.parseField(entityType, searchCriteria.getKey().getNameField());

            switch (searchCriteria.getOperation()) {
                case EQUALITY:
                    groupExpressions.add(criteriaBuilder.equal(getAggregateExpression(searchCriteria.getKey()),
                                                 searchCriteria.getParsedValue(field.getFieldTypeName())));
                    continue;
                case NEGATION:
                    groupExpressions.add(criteriaBuilder.notEqual(getAggregateExpression(searchCriteria.getKey()),
                                                    searchCriteria.getParsedValue(field.getFieldTypeName())));
                    continue;
                case GREATER_THAN:
                    groupExpressions.add(criteriaBuilder.greaterThan(getAggregateExpression(searchCriteria.getKey()),
                                                       searchCriteria.getParsedValue(field.getFieldTypeName())));
                    continue;
                case LESS_THAN:
                    groupExpressions.add(criteriaBuilder.lessThan(getAggregateExpression(searchCriteria.getKey()),
                                                    searchCriteria.getParsedValue(field.getFieldTypeName())));
                    continue;
                default:
                    throw new RuntimeException("Comparator not allowed");
            }
        }
        return groupExpressions;
    }

    public Expression getAggregateExpression(ProjectionField projection) throws NoSuchFieldException {
        ParsedField filter = FieldParser.parseField(entityType, projection.getNameField());
        From query = FieldParser.joinOrGetJoinedExpression(root, entityType, projection.getNameField());

        Expression expression = query.get(filter.getFieldName());
        expression.alias(projection.getNameField());

        if(projection.getProjectionType() == ProjectionType.AVERAGE)
            expression = criteriaBuilder.avg(expression);
        if(projection.getProjectionType() == ProjectionType.SUM)
            expression = criteriaBuilder.sum(expression);
        if(projection.getProjectionType() == ProjectionType.COUNT)
            expression = criteriaBuilder.count(expression);
        if(projection.getProjectionType() == ProjectionType.DISTINCT_COUNT)
            expression = criteriaBuilder.countDistinct(expression);

        return expression;
    }

    public CriteriaQuery getHavingClause(List<SearchCriteria> filters, CriteriaQuery query)
            throws NoSuchFieldException, ParseException {
        if(filters != null && filters.size() > 0){
            List<Predicate> havingClausePredicates = toSelection(filters);
            havingClausePredicates.forEach(query::having);
            return query;
        }
        return query;
    }
}
