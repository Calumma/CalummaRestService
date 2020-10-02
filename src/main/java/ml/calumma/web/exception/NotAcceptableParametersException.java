package ml.calumma.web.exception;

import ml.calumma.web.model.request.RequestError;

public class NotAcceptableParametersException extends Exception {

    private RequestError request;

    public NotAcceptableParametersException(String message) {
        super(message);
    }

    public NotAcceptableParametersException(RequestError request){
        this.request = request;
    }

    public RequestError getRequest() {
        return request;
    }
}
