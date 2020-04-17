package com.calumma.backend.web.repository.core.projections;

import com.calumma.backend.model.entity.CalummaEntity;
import com.calumma.backend.web.repository.core.symbol.FieldParser;
import com.calumma.backend.web.repository.core.symbol.ParsedField;
import com.calumma.backend.web.repository.core.symbol.ProjectionField;
import com.calumma.backend.web.repository.core.symbol.ProjectionType;

import javax.persistence.criteria.*;
import java.lang.reflect.Type;

public class CoreEntityProjection<Entity extends CalummaEntity> {

    private Class rootEntity;
    private Root<Entity> root;
    private CriteriaBuilder criteriaBuilder;

    CoreEntityProjection(Root<Entity> root, Class rootEntity, CriteriaBuilder criteriaBuilder) {
        this.root = root;
        this.rootEntity = rootEntity;
        this.criteriaBuilder = criteriaBuilder;
    }

    public Expression toSelection (ProjectionField projection) throws NoSuchFieldException {

        ParsedField filter = FieldParser.parseField(rootEntity, projection.getNameField());
        From query = FieldParser.joinOrGetJoinedExpression(root, rootEntity, projection.getNameField());

        Type field = FieldParser.getTypeOfColumn(rootEntity, projection.getNameField());

        Expression expression = query.get(filter.getFieldName());

        if(CalummaEntity.class.isAssignableFrom((Class) field)){
            expression = query.join(filter.getFieldName(), JoinType.LEFT);
        }

        if((CalummaEntity.class.isAssignableFrom((Class) FieldParser.getTypeOfColumn(rootEntity, projection.getNameField()))))
            expression = query.join(filter.getFieldName());

        if(projection.getProjectionType() == ProjectionType.AVERAGE)
            expression = criteriaBuilder.avg(expression);
        if(projection.getProjectionType() == ProjectionType.SUM)
            expression = criteriaBuilder.sum(expression);
        if(projection.getProjectionType() == ProjectionType.COUNT)
            expression = criteriaBuilder.count(expression);
        if(projection.getProjectionType() == ProjectionType.DISTINCT_COUNT)
            expression = criteriaBuilder.countDistinct(expression);
        if(projection.getProjectionType() == ProjectionType.MAX)
            expression = criteriaBuilder.max(expression);
        if(projection.getProjectionType() == ProjectionType.MIN)
            expression = criteriaBuilder.min(expression);

        return expression;
    }

}
