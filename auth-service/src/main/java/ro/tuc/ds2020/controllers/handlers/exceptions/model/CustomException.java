package ro.tuc.ds2020.controllers.handlers.exceptions.model;

import org.springframework.http.HttpStatus;

import java.util.List;

public class CustomException extends RuntimeException {
    private final String resource;
    private final HttpStatus status;
    private final List<String> validationErrors;

    public CustomException(String message, HttpStatus status, String resource, List<String> validationErrors) {
        super(message);
        this.resource = resource;
        this.validationErrors = validationErrors;
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public List<String> getValidationErrors() {
        return validationErrors;
    }

    public String getResource() {
        return resource;
    }
}
