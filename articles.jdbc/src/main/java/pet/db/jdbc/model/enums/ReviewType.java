package pet.db.jdbc.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.NoSuchElementException;

@Getter
@RequiredArgsConstructor
public enum ReviewType {

    POSITIVE(0),
    NEUTRAL(1),
    CRITICAL(2);

    private final int number;

    public static ReviewType getByNumber(Integer number) {
        return Arrays
                .stream(ReviewType.values())
                .filter(x -> x.getNumber() == number).
                findFirst()
                .orElseThrow(NoSuchElementException::new);
    }

}
