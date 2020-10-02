package ml.calumma.rest.controller;

import ml.calumma.exception.ForbiddenException;
import ml.calumma.model.entity.CalummaEntity;
import ml.calumma.model.request.ClientRequest;
import ml.calumma.model.request.ParsedRequest;
import ml.calumma.model.request.RequestQueryConfig;
import ml.calumma.rest.service.BaseService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public abstract class BaseController<Entity extends CalummaEntity, T> {

    public abstract BaseService<Entity, T> getService();

    public abstract List<RequestQueryConfig> getQueryRequestConfiguration();

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public Entity getById(@PathVariable("id") Long id) throws Throwable {
        return getService().getById(id);
    }

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public Page<Entity> getList(@RequestParam(required = false) String filters, Pageable pageInfo) {
        ParsedRequest parsedRequest = new ParsedRequest(getQueryRequestConfiguration());
        parsedRequest.buildRequest(filters, pageInfo);
        return getService().list(parsedRequest);
    }

    @RequestMapping(value = "/query", method = RequestMethod.GET)
    public Page<Entity> queryEntity(@RequestParam(required = false) String filters,
                                    @RequestParam(required = false) String projection,
                                    Pageable pageInfo) throws Throwable {
        ParsedRequest parsedRequest = new ParsedRequest(getQueryRequestConfiguration());
        parsedRequest.buildRequest(projection, filters, pageInfo);
        return getService().query(parsedRequest);
    }

    @RequestMapping(value = "/query", method = RequestMethod.POST)
    public Page<Entity> queryEntityPost(@RequestBody ClientRequest clientRequest) throws Throwable {
        ParsedRequest parsedRequest = new ParsedRequest(getQueryRequestConfiguration());
        parsedRequest.buildRequest(clientRequest);
        return getService().query(parsedRequest);
    }

    @RequestMapping(value = "", method = RequestMethod.POST)
    public Entity create(@RequestBody Entity entity) throws Exception {
        return getService().insert(entity);
    }

    @RequestMapping(value = "", method = RequestMethod.PUT)
    public Entity update(@RequestBody Entity entity) throws Exception {
        return getService().update(entity);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public void delete(@PathVariable("id") T id) throws ForbiddenException {
        getService().delete(id);
    }
}
