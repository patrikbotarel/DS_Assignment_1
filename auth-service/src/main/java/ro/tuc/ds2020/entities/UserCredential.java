package ro.tuc.ds2020.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import org.springframework.data.domain.Persistable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "user_credentials")
public class UserCredential implements Serializable, Persistable<UUID> {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id", columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role;

    @Transient
    private boolean isNewRecord = false;

    public UserCredential() {
        this.isNewRecord = false;
    }

    public UserCredential(final String username, final String password, final UserRole role) {
        this.id = UUID.randomUUID();
        this.username = username;
        this.password = password;
        this.role = role;
        this.isNewRecord = true;
    }

    public UserCredential(final UUID id, final String username, final String password, final UserRole role) {
        this.id = id != null ? id : UUID.randomUUID();
        this.username = username;
        this.password = password;
        this.role = role;
        this.isNewRecord = true;
    }

    @Override
    public UUID getId() {
        return id;
    }

    public void setId(final UUID id) {
        this.id = id;
    }

    @Override
    public boolean isNew() {
        return this.isNewRecord;
    }

    public void setNewRecord(final boolean isNewRecord) {
        this.isNewRecord = isNewRecord;
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
        this.role = role;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserCredential that = (UserCredential) o;
        return Objects.equals(id, that.id)
                && Objects.equals(username, that.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username);
    }
}
