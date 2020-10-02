package ml.calumma.web.web.repository.core.aggregation;

import ml.calumma.web.model.entity.CalummaEntity;
import ml.calumma.web.web.repository.core.symbol.FieldParser;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

public class CoreEntityGrouper<Entity extends CalummaEntity> {

    private Root<Entity> root;
    private Class entityType;

    public CoreEntityGrouper(Root<Entity> root, Class entityType) {
        this.root = root;
        this.entityType = entityType;
    }

    private List<Expression> toSelection(List<String> groupBy) throws NoSuchFieldException {
        List<Expression> groupExpressions = new ArrayList<>();

        for(String groupField: groupBy){
            From query = FieldParser.joinOrGetJoinedExpression(root, entityType, groupField);

            Expression expression = query.get(FieldParser.getColumnName(groupField));
            expression.alias(groupField);
            groupExpressions.add(expression);
        }

        return groupExpressions;
    }

    public CriteriaQuery getGroupByClause(List<String> groupBy, CriteriaQuery query) throws NoSuchFieldException {
        if(groupBy != null){
            List<Expression> groupByClauseExpression = toSelection(groupBy);
            return query.groupBy(groupByClauseExpression);
        }
        return query;
    }
}
