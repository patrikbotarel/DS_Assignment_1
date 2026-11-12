package ro.tuc.ds2020.dtos;

import ro.tuc.ds2020.entities.UserRole;

import java.util.Objects;
import java.util.UUID;

public class UserDTO {
    private UUID id;
    private String username;
    private String name;
    private UserRole role;

    public UserDTO() {
    }

    public UserDTO(UUID id, String username, String name, UserRole role) {
        this.id = id;
        this.username = username;
        this.name = name;
        this.role = role;
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
        this.role = role;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserDTO userDTO = (UserDTO) o;
        return Objects.equals(id, userDTO.id)
                && Objects.equals(username, userDTO.username)
                && role == userDTO.role;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username, role);
    }
}
