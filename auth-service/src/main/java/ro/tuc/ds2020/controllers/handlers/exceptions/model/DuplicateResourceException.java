package ro.tuc.ds2020.controllers.handlers.exceptions.model;

import org.springframework.http.HttpStatus;

import java.util.ArrayList;

public class DuplicateResourceException extends CustomException {
    private static final String MESSAGE = "Resource already exists!";
    private static final HttpStatus STATUS = HttpStatus.CONFLICT;

    public DuplicateResourceException(String resource) {
        super(MESSAGE, STATUS, resource, new ArrayList<>());
    }
}
