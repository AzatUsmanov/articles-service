package pet.db.jdbc.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.NoSuchElementException;

@Getter
@RequiredArgsConstructor
public enum UserRole {

    ROLE_USER(0),
    ROLE_ADMIN(1);

    private final int number;

    public static UserRole getByNumber(Integer number) {
        return Arrays
                .stream(UserRole.values())
                .filter(x -> x.getNumber() == number).
                findFirst()
                .orElseThrow(NoSuchElementException::new);
    }

}
