package pet.db.jdbc.model.dto.payload;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import pet.db.jdbc.model.enums.UserRole;

public record UserPayload(
        @NotNull(message = "username must be not null")
        @Size(min = USERNAME_MIN_LENGTH, max = USERNAME_MAX_LENGTH,
                message = "the length of the username must be between {min} and {max}")
        String username,

        @NotNull(message = "email must be not null")
        @Size(min = EMAIL_MIN_LENGTH, max = EMAIL_MAX_LENGTH,
                message = "the length of the email must be between {min} and {max}")
        @Email(message = "the email is in the wrong format")
        String email,

        @NotNull(message = "password must be not null")
        @Size(min = PASSWORD_MIN_LENGTH, max = USERNAME_MAX_LENGTH,
                message = "the length of the password must be between {min} and {max}")
        String password,

        @NotNull(message = "User role must be not null")
        UserRole role) {

        public static final int USERNAME_MIN_LENGTH = 5;

        public static final int USERNAME_MAX_LENGTH = 30;

        public static final int EMAIL_MIN_LENGTH = 5;

        public static final int EMAIL_MAX_LENGTH = 50;

        public static final int PASSWORD_MIN_LENGTH = 5;

        public static final int PASSWORD_MAX_LENGTH = 50;

}
