package ml.calumma.exception;

import ml.calumma.model.request.RequestError;

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
