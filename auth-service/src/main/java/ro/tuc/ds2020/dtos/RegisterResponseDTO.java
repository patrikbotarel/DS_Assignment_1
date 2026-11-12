package ro.tuc.ds2020.dtos;

import ro.tuc.ds2020.entities.UserRole;

import java.util.UUID;

public class RegisterResponseDTO {

    private UUID id;
    private String username;
    private UserRole role;
    private String message;

    public RegisterResponseDTO() {
    }

    public RegisterResponseDTO(UUID id, String username, UserRole role, String message) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.message = message;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
