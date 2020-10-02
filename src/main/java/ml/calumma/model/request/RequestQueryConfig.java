package ml.calumma.model.request;

import ml.calumma.rest.repository.core.symbol.SearchOperation;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;

public class RequestQueryConfig {

    @JsonIgnore
    private String fieldName;

    private String projection;
    private List<SearchOperation> allowedOperations;

    public RequestQueryConfig(String fieldName, String projection, List<SearchOperation> allowedOperations) {
        this.fieldName = fieldName;
        this.projection = projection;
        this.allowedOperations = allowedOperations;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getProjection() {
        return projection;
    }

    public void setProjection(String projection) {
        this.projection = projection;
    }

    public List<SearchOperation> getAllowedOperations() {
        return allowedOperations;
    }

    public void setAllowedOperations(List<SearchOperation> allowedOperations) {
        this.allowedOperations = allowedOperations;
    }
}
