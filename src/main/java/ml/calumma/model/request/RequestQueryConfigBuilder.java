package ml.calumma.model.request;

import ml.calumma.rest.repository.core.symbol.SearchOperation;

import java.util.ArrayList;
import java.util.List;

public class RequestQueryConfigBuilder {

    private List<RequestQueryConfig> requestQueryConfigList;

    public RequestQueryConfigBuilder() {
        this.requestQueryConfigList = new ArrayList<>();
    }

    public List<RequestQueryConfig> build() {
        return requestQueryConfigList;
    }

    public final RequestQueryConfigBuilder with(final String fieldName, final  String projection,
                                                final  List<SearchOperation> allowedOperations) {

        requestQueryConfigList.add(new RequestQueryConfig(fieldName, projection, allowedOperations));
        return this;
    }

}
