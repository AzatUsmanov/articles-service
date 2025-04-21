package pet.db.jdbc.tool.exception;

import lombok.Getter;

@Getter
public class DuplicateUserException extends Exception {

    private final String fieldName;

    public DuplicateUserException(String fieldName, String message) {
        super(message);
        this.fieldName = fieldName;
    }

}
