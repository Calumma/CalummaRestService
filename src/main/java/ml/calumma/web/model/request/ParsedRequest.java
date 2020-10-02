package ml.calumma.web.model.request;

import ml.calumma.web.exception.NotAcceptableParametersException;
import ml.calumma.web.web.repository.core.symbol.ProjectionField;
import ml.calumma.web.web.repository.core.symbol.ProjectionType;
import ml.calumma.web.web.repository.core.symbol.SearchCriteria;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ParsedRequest {

    @JsonIgnore
    private List<RequestQueryConfig> requestQueryConfigs;

    private List<ProjectionField> projections;

    private List<SearchCriteria> filters;
    private List<String> groupBy;
    private Pageable pageable;

    public ParsedRequest(List<ProjectionField> projections, List<SearchCriteria> filters) {
        this.projections = projections;
        this.filters = filters;
    }

    public ParsedRequest(List<RequestQueryConfig> requestQueryConfigs) {
        this.groupBy = new ArrayList<>();
        this.filters = new ArrayList<>();
        this.projections = new ArrayList<>();

        this.requestQueryConfigs = requestQueryConfigs;
    }

    private String findFieldNameByAlias(String alias) {
        Optional<String> potentialField = requestQueryConfigs.stream().filter(x -> x.getProjection().equals(alias))
                .map(RequestQueryConfig::getFieldName).findFirst();

        if (potentialField.isPresent())
            return potentialField.get();

        return alias;
    }

    public void buildRequest(String projections, String filters, Pageable pageable)
            throws NotAcceptableParametersException {
        this.pageable = pageable;

        if (projections != null && !projections.isEmpty() && !projections.isBlank()) {
            Arrays.asList(projections.split(","))
                    .forEach(projection ->
                    {
                        ProjectionField projectionField = new ProjectionField(findFieldNameByAlias(projection), projection);
                        this.projections.add(projectionField);
                        if(projectionField.getProjectionType() == ProjectionType.SIMPLE) {
                            this.groupBy.add(projectionField.getNameField());
                        }
                    });
        }

        if (filters != null && !filters.isEmpty() && !filters.isBlank()) {

            for(String filter: filters.split(" and ")){
                SearchCriteria searchCriteria = new SearchCriteria(filter);
                ProjectionField field =  searchCriteria.getKey();
                field.setAlias(field.getNameField());
                field.setNameField(findFieldNameByAlias(field.getNameField()));
                searchCriteria.setKey(field);
                this.filters.add(searchCriteria);
            }
        }

        ParsedRequest potentialErrors = findRequestErrors();

        if(potentialErrors != null) {
            if (potentialErrors.projections.size() > 0 || potentialErrors.filters.size() > 0) {
                throw new NotAcceptableParametersException(new RequestError(potentialErrors.projections, potentialErrors.filters));
            }
        }
    }

    public void buildRequest(ClientRequest clientRequest){
        this.projections = new ArrayList<>();

        if(clientRequest.getFilters() != null) {
            this.filters = clientRequest.getFilters().stream().map(x ->
                    new SearchCriteria(new ProjectionField(findFieldNameByAlias(x.getField()), x.getField()),
                            x.getValue(), x.getOperation()))
                    .collect(Collectors.toList());
        }

        if(clientRequest.getProjections() != null) {
            clientRequest.getProjections()
                    .forEach(projection ->
                            this.projections.add(new ProjectionField(findFieldNameByAlias(projection), projection)));
        }

        if(clientRequest.getPageRequest() != null)
            this.pageable = clientRequest.getPageRequest();
        else
            this.pageable = PageRequest.of(0, 20);

    }

    public void buildRequest(String filters, Pageable pageable) {

        this.pageable = pageable;

        if (filters != null && !filters.isEmpty() && !filters.isBlank()) {
            this.filters = new ArrayList<>();
            Arrays.stream(filters.split(" and "))
                    .filter(filter -> !ProjectionField.isAggregation(filter))
                    .forEach(filter -> this.filters.add(new SearchCriteria(filter)));
        }
    }

    public ParsedRequest findRequestErrors() {

        List<SearchCriteria> invalidFilters = new ArrayList<>();
        List<ProjectionField> invalidProjections = new ArrayList<>();

        if (projections != null) {
            invalidProjections = projections.stream()
                    .filter(projectionField -> requestQueryConfigs.stream().map(RequestQueryConfig::getProjection)
                            .noneMatch(x -> x.equals(projectionField.getAlias()))).collect(Collectors.toList());

        }

        if (filters != null) {
            invalidFilters = filters.stream().filter(searchCriteria -> requestQueryConfigs.stream()
                    .noneMatch(config -> config.getAllowedOperations().contains(searchCriteria.getOperation()) &&
                            config.getProjection().equals(searchCriteria.getKey().getAlias())))
                    .collect(Collectors.toList());
        }

        if (invalidFilters.size() > 0 || invalidProjections.size() > 0)
            return new ParsedRequest(invalidProjections, invalidFilters);

        return null;
    }

    public List<ProjectionField> getProjections() {
        return projections;
    }

    public List<ProjectionField> getNotEagerProjections() {
        return projections.stream().filter(x -> !x.getProjectionType().equals(ProjectionType.EAGER))
                .collect(Collectors.toList());
    }


    public ParsedRequest setProjections(List<ProjectionField> projections) {
        this.projections = projections;
        return this;
    }

    public List<SearchCriteria> getFilters() {
        if (filters == null)
            return null;
        return filters.stream().filter(filter -> filter.getKey().getProjectionType() == ProjectionType.SIMPLE)
                .collect(Collectors.toList());
    }

    public ParsedRequest setFilters(List<SearchCriteria> filters) {
        this.filters = filters;
        return this;
    }

    @JsonIgnore
    public List<String> getGroupBy() {
        return groupBy;
    }

    public ParsedRequest setGroupBy(List<String> groupBy) {
        this.groupBy = groupBy;
        return this;
    }

    @JsonIgnore
    public Pageable getPageable() {
        return pageable;
    }

    public ParsedRequest setPageable(Pageable pageable) {
        this.pageable = pageable;
        return this;
    }

    public List<SearchCriteria> getAggregationFilters() {
        if (filters == null)
            return null;
        return filters.stream().filter(filter -> filter.getKey().getProjectionType() != ProjectionType.SIMPLE)
                .collect(Collectors.toList());
    }
}


