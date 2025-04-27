package pet.db.jdbc.model.enums;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum UserColumn {

    ID("id"),
    USERNAME("username"),
    EMAIL("email"),
    PASSWORD("password"),
    ROLE("role");

    private final String name;

    @Override
    public String toString() {
        return name;
    }

}
