package ro.tuc.ds2020.entities;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum UserRole {
    ADMIN,
    CLIENT;

    @JsonCreator
    public static UserRole fromString(final String value) {
        if (value == null) {
            return CLIENT;
        }
        for (UserRole role : values()) {
            if (role.name().equalsIgnoreCase(value.trim())) {
                return role;
            }
        }
        return CLIENT;
    }
}
