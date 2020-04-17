package com.calumma.backend.web.repository.core.restrictions;

import com.calumma.backend.model.entity.CalummaEntity;
import com.calumma.backend.web.repository.core.symbol.SearchCriteria;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class CoreEntitySpecificationBuilder<Entity extends CalummaEntity> {

    private final List<SearchCriteria> params;
    private final Class rootEntity;

    public CoreEntitySpecificationBuilder(Class rootEntity) {
        params = new ArrayList<>();
        this.rootEntity = rootEntity;
    }

    public Specification<Entity> build() {

        if(params.size() > 0) {
            Specification<Entity> result = new CoreEntitySpecification<Entity>(params.remove(0), rootEntity);
            for(SearchCriteria param : params){
                result = Specification.where(result).and(new CoreEntitySpecification<Entity>(param, rootEntity));
            }
            return result;
        }
        return null;
    }

    public final CoreEntitySpecificationBuilder with(final SearchCriteria criteria) {
        params.add(criteria);
        return this;
    }
}
