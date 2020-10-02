package ml.calumma.web.model.request;

import ml.calumma.web.web.repository.core.symbol.SearchOperation;

public class ClientSearchCriteria {
    private String field;
    private String value;
    private SearchOperation operation;

    public String getField() {
        return field;
    }

    public ClientSearchCriteria setField(String field) {
        this.field = field;
        return this;
    }

    public String getValue() {
        return value;
    }

    public ClientSearchCriteria setValue(String value) {
        this.value = value;
        return this;
    }

    public SearchOperation getOperation() {
        return operation;
    }

    public ClientSearchCriteria setOperation(SearchOperation operation) {
        this.operation = operation;
        return this;
    }
}
