package com.calumma.backend.model.request;

import org.springframework.data.domain.PageRequest;

import java.util.List;

public class ClientRequest {

    private List<String> projections;
    private List<ClientSearchCriteria> filters;
    private PageRequest pageRequest;

    public List<String> getProjections() {
        return projections;
    }

    public ClientRequest setProjections(List<String> projections) {
        this.projections = projections;
        return this;
    }

    public List<ClientSearchCriteria> getFilters() {
        return filters;
    }

    public ClientRequest setFilters(List<ClientSearchCriteria> filters) {
        this.filters = filters;
        return this;
    }

    public PageRequest getPageRequest() {
        return pageRequest;
    }

    public ClientRequest setPageRequest(PageRequest pageRequest) {
        this.pageRequest = pageRequest;
        return this;
    }
}
