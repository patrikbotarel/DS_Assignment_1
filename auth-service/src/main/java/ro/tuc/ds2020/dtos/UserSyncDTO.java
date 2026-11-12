package ro.tuc.ds2020.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import ro.tuc.ds2020.entities.UserRole;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserSyncDTO {

    private UUID id;
    private String username;
    private String password;
    private UserRole role = UserRole.CLIENT;

    public UserSyncDTO() {
    }

    public UserSyncDTO(final UUID id, final String username, final String password, final UserRole role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role != null ? role : UserRole.CLIENT;
    }

    public UUID getId() {
        return id;
    }

    public void setId(final UUID id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(final UserRole role) {
        this.role = role != null ? role : UserRole.CLIENT;
    }
}
