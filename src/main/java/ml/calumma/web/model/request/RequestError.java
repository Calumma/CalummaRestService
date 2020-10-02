package ml.calumma.web.model.request;
import ml.calumma.web.web.repository.core.symbol.ProjectionField;
import ml.calumma.web.web.repository.core.symbol.SearchCriteria;

import java.util.List;
import java.util.stream.Collectors;

public class RequestError {

    private String message;
    private List<String> projections;
    private List<SearchCriteria> filters;

    public RequestError(List<ProjectionField> projections, List<SearchCriteria> filters) {
        this.message = "Request not supported. Review those parameters:";
        this.projections = projections.stream().map(ProjectionField::getNameField).collect(Collectors.toList());
        this.filters = filters;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<String> getProjections() {
        return projections;
    }

    public void setProjections(List<String> projections) {
        this.projections = projections;
    }

    public List<SearchCriteria> getFilters() {
        return filters;
    }

    public void setFilters(List<SearchCriteria> filters) {
        this.filters = filters;
    }
}
