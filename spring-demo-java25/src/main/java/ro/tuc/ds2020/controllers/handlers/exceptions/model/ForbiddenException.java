package ro.tuc.ds2020.controllers.handlers.exceptions.model;

import org.springframework.http.HttpStatus;

import java.util.ArrayList;

public class ForbiddenException extends CustomException {
    private static final String MESSAGE = "Access forbidden!";
    private static final HttpStatus STATUS = HttpStatus.FORBIDDEN;

    public ForbiddenException(String resource) {
        super(MESSAGE, STATUS, resource, new ArrayList<>());
    }
}
