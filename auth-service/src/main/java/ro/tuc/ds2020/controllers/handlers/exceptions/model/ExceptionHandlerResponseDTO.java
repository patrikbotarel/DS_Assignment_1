package ro.tuc.ds2020.controllers.handlers.exceptions.model;

import java.util.Date;
import java.util.List;

public class ExceptionHandlerResponseDTO {
    private Date timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    private List<String> details;

    public ExceptionHandlerResponseDTO(String error, String message, int status, String resource, List<String> details, String path) {
        this.timestamp = new Date();
        this.error = error;
        this.message = message;
        this.status = status;
        this.details = details;
        this.path = path;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<String> getDetails() {
        return details;
    }

    public void setDetails(List<String> details) {
        this.details = details;
    }
}
