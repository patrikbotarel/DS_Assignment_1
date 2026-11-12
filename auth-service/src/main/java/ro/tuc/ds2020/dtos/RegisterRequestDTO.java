package ro.tuc.ds2020.dtos;

import jakarta.validation.constraints.NotBlank;
import ro.tuc.ds2020.entities.UserRole;

public class RegisterRequestDTO {

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;

    private String name;

    private UserRole role = UserRole.CLIENT;

    public RegisterRequestDTO() {
    }

    public RegisterRequestDTO(String username, String password, String name, UserRole role) {
        this.username = username;
        this.password = password;
        this.name = name;
        this.role = role != null ? role : UserRole.CLIENT;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role != null ? role : UserRole.CLIENT;
    }
}
