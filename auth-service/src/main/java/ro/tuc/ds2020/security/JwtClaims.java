package ro.tuc.ds2020.security;

import java.util.UUID;

public class JwtClaims {

    private final String username;
    private final UUID userId;
    private final String role;
    private final long expiration;

    public JwtClaims(String username, UUID userId, String role, long expiration) {
        this.username = username;
        this.userId = userId;
        this.role = role;
        this.expiration = expiration;
    }

    public String getUsername() {
        return username;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getRole() {
        return role;
    }

    public long getExpiration() {
        return expiration;
    }
}
