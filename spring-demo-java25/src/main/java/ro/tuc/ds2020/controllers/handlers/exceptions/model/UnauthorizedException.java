package ro.tuc.ds2020.controllers.handlers.exceptions.model;

import org.springframework.http.HttpStatus;

import java.util.ArrayList;

public class UnauthorizedException extends CustomException {
    private static final String MESSAGE = "Unauthorized access!";
    private static final HttpStatus STATUS = HttpStatus.UNAUTHORIZED;

    public UnauthorizedException(String resource) {
        super(MESSAGE, STATUS, resource, new ArrayList<>());
    }
}
