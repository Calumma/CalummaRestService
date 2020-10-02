package ml.calumma.rest.repository.core.symbol;

import java.lang.reflect.Type;

public class ParsedField {

    private String fieldName;
    private Type fieldType;

    public String getFieldTypeName(){
        return fieldType.getTypeName();
    }

    public ParsedField(String fieldName, Type fieldType) {
        this.fieldName = fieldName;
        this.fieldType = fieldType;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public Type getFieldType() {
        return fieldType;
    }

    public void setFieldType(Type fieldType) {
        this.fieldType = fieldType;
    }
}
