package ml.calumma.rest.repository.core.symbol;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;


public class SearchCriteria <T extends Comparable<T>> {

    private ProjectionField key;
    private T value;
    private SearchOperation operation;

    public SearchCriteria(String filter){
        String[] filters = filter.split("__");
        if(filters.length != 3)
            throw new RuntimeException("Filter with bad formation => " + filter);

        this.key = new ProjectionField(filters[0]);
        this.operation = SearchOperation.getSimpleOperation(filters[1]);
        this.value = (T) filters[2];
    }

    public SearchCriteria(String key, T value, SearchOperation operation) {
        this.key = new ProjectionField(key);
        this.value = value;
        this.operation = operation;
    }

    public SearchCriteria(ProjectionField key, String value, SearchOperation operation) {
        this.key = key;
        this.value = (T) value;
        this.operation = operation;
    }

    public T getValueGeneric(Object type){
        return null;
    }

    public String getKeyName() {
        return key.getNameField();
    }

    @JsonIgnore
    public ProjectionField getKey() {
        return key;
    }

    public void setKey(ProjectionField key) {
        this.key = key;
    }

    public T getValue() {
        return value;
    }

    public Comparable getParsedValue(String typeOfColumn) throws ParseException {
        DateFormat formatFullDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        DateFormat formatWithoutHour = new SimpleDateFormat("yyyy-MM-dd");

        switch (typeOfColumn){
            case "long":
                return Float.parseFloat((String) value);
            case "int":
                return Integer.parseInt((String) value);
            case "boolean":
                return "true".equals(value);
            case "Date":
                try {
                    return formatFullDate.parse((String) value);
                } catch (Exception e){
                    return formatWithoutHour.parse((String) value);
                }
            case "java.util.Calendar":
                Calendar calendar = Calendar.getInstance();
                try {
                    calendar.setTime(formatFullDate.parse((String) value));
                    return calendar;
                } catch (Exception e){
                    calendar.setTime( formatWithoutHour.parse((String) value));
                    return calendar;
                }
            default:
                return value;
        }

    }

    public void setValue(T value) {
        this.value = value;
    }

    public SearchOperation getOperation() {
        return operation;
    }

    public void setOperation(SearchOperation operation) {
        this.operation = operation;
    }
}
