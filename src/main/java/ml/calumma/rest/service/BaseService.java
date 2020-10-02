package ml.calumma.rest.service;

import ml.calumma.model.entity.CalummaEntity;
import ml.calumma.exception.ForbiddenException;
import ml.calumma.rest.repository.BaseRepository;
import ml.calumma.rest.repository.core.DynamicQueryRepository;
import ml.calumma.rest.repository.core.restrictions.CoreEntitySpecificationBuilder;
import ml.calumma.rest.repository.core.symbol.SearchCriteria;
import ml.calumma.rest.repository.core.symbol.SearchOperation;
import ml.calumma.model.request.ParsedRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;

import java.text.ParseException;
import java.util.Calendar;
import java.util.List;

public abstract class BaseService<Entity extends CalummaEntity, T> {

    protected abstract BaseRepository<Entity, T> getRepository();
    protected abstract Class getBaseEntityType();

    protected abstract boolean userHasAccessToData(Entity entity);
    protected abstract Entity setUserSignature(Entity entity);
    protected abstract SearchCriteria getFilterUserRestriction();
    protected abstract boolean userNeedsReadPermission();

    private DynamicQueryRepository dynamicQueryRepository;

    public Entity getById(Long id) throws Throwable {
        Entity entity =  getRepository().findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("No entity found with id: " + id));

        if(userHasAccessToData(entity) || !userNeedsReadPermission())
            return entity;

        throw new ForbiddenException();
    }

    public Entity insert(Entity entity) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        System.out.println(objectMapper.writeValueAsString(entity));
        entity.setInsertDate(Calendar.getInstance());
        entity = setUserSignature(entity);
        return getRepository().save(entity);
    }

    public Entity update(Entity entity) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        getRepository().flush();
        Entity dbEntity = getRepository().findById((T) entity.getId()).get();

        if(userHasAccessToData(entity)) {
            ObjectReader updater = objectMapper.readerForUpdating(dbEntity);
            Entity merged = updater.readValue(objectMapper.writeValueAsString(entity));
            return getRepository().save(merged);
        }

        throw new ForbiddenException();
    }

    public void delete(T id) throws ForbiddenException {
        Entity toDelete = getRepository().getOne(id);

        if(userHasAccessToData(toDelete)) {
            toDelete.setDeletionDate(Calendar.getInstance());
            toDelete.setDeleted(true);
            getRepository().save(toDelete);
        }else{
            throw new ForbiddenException();
        }
    }

    public Page<Entity> list(ParsedRequest parsedRequest){

        Specification<Entity> specifications = parseUrlToExpressions(parsedRequest.getFilters());
        return getRepository().findAll(specifications, parsedRequest.getPageable());
    }

    public Page<Entity> query(ParsedRequest parsedRequest) throws NoSuchFieldException, ParseException, IllegalAccessException {

        Specification<Entity> specifications = parseUrlToExpressions(parsedRequest.getFilters());
        return dynamicQueryRepository.queryBy(getBaseEntityType(), specifications, parsedRequest);
    }

    private Specification<Entity> parseUrlToExpressions(List<SearchCriteria> filters) {
        CoreEntitySpecificationBuilder<Entity> skeepEntitySpecificationBuilder =
                new CoreEntitySpecificationBuilder<Entity>(getBaseEntityType());

        if(filters != null && !filters.isEmpty()) {
            filters.forEach(skeepEntitySpecificationBuilder::with);
        }

        skeepEntitySpecificationBuilder
                .with(new SearchCriteria("isDeleted", false, SearchOperation.EQUALITY));

        if(userNeedsReadPermission())
            skeepEntitySpecificationBuilder.with(getFilterUserRestriction());

        return skeepEntitySpecificationBuilder.build();
    }

    @Autowired
    public void setDynamicQueryRepository(DynamicQueryRepository dynamicQueryRepository) {
        this.dynamicQueryRepository = dynamicQueryRepository;
    }

}