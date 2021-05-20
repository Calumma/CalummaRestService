package ml.calumma.rest.repository.core.restrictions;

import ml.calumma.model.entity.CalummaEntity;
import ml.calumma.rest.repository.core.symbol.FieldParser;
import ml.calumma.rest.repository.core.symbol.ParsedField;
import ml.calumma.rest.repository.core.symbol.SearchCriteria;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.util.Calendar;

public class CoreEntitySpecification<Entity extends CalummaEntity> implements Specification<Entity> {

    private SearchCriteria searchCriteria;
    private Class rootEntity;

    CoreEntitySpecification(SearchCriteria searchCriteria, Class rootEntity) {
        this.searchCriteria = searchCriteria;
        this.rootEntity = rootEntity;
    }

    @Override
    public Predicate toPredicate(Root<Entity> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {

        try {
            ParsedField field = FieldParser.parseField(rootEntity, searchCriteria.getKey().getNameField());

            String columnName = field.getFieldName();
            From query = FieldParser.joinOrGetJoinedExpression(root, rootEntity,  searchCriteria.getKey().getNameField());
            String typeOfColumn = field.getFieldTypeName();
            Type type = field.getFieldType();

            switch (searchCriteria.getOperation()) {
                case EQUALITY:
                    return criteriaBuilder.equal(query.get(columnName), searchCriteria.getParsedValue(type));
                case NEGATION:
                    return criteriaBuilder.notEqual(query.get(columnName), searchCriteria.getParsedValue(type));
                case GREATER_THAN:
                    return criteriaBuilder.greaterThan(query.get(columnName),
                            searchCriteria.getParsedValue(type));
                case LESS_THAN:
                    return criteriaBuilder.lessThan(query.get(columnName), searchCriteria.getParsedValue(type));
                case LIKE:
                    Comparable likeField = searchCriteria.getParsedValue(type);
                    if(likeField instanceof Calendar){
                        Calendar startOfDay = (Calendar) searchCriteria.getParsedValue(type);

                        Calendar endOfDay = Calendar.getInstance();
                        endOfDay.setTime(startOfDay.getTime());
                        endOfDay.set(Calendar.HOUR_OF_DAY, 23);
                        endOfDay.set(Calendar.MINUTE, 59);
                        endOfDay.set(Calendar.SECOND, 59);
                        endOfDay.set(Calendar.MILLISECOND, 999);

                        return criteriaBuilder.between(query.get(columnName), startOfDay, endOfDay);
                    }
                    return criteriaBuilder.like(
                            criteriaBuilder.lower(query.get(columnName)),
                            String.format("%%%s%%", searchCriteria.getValue()).toLowerCase());
                case IN:
                    return query.get(columnName).in(searchCriteria.getValue().toString().split(","));
                default:
                    throw new RuntimeException("Comparator not allowed");
            }

        } catch (NoSuchFieldException | ParseException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("ERROR");
    }
}
