package ro.tuc.ds2020.dtos;

import java.util.UUID;

public class ValidateResponseDTO {

    private boolean valid;
    private UUID userId;
    private String username;
    private String role;

    public ValidateResponseDTO() {
    }

    public ValidateResponseDTO(boolean valid, UUID userId, String username, String role) {
        this.valid = valid;
        this.userId = userId;
        this.username = username;
        this.role = role;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
