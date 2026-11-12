package ro.tuc.ds2020.controllers.handlers.exceptions.model;

import org.springframework.http.HttpStatus;

import java.util.ArrayList;

public class ParameterValidationException extends CustomException {
    private static final String MESSAGE = "Parameter is invalid!";
    private static final HttpStatus STATUS = HttpStatus.BAD_REQUEST;

    public ParameterValidationException(String resource) {
        super(MESSAGE, STATUS, resource, new ArrayList<>());
    }
}
