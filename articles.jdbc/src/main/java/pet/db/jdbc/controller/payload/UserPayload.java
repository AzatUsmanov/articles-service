package pet.db.jdbc.controller.payload;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pet.db.jdbc.entity.User;

public record UserPayload(
        @NotNull(message = "username must be not null")
        @Size(min = UserPayload.USERNAME_MIN_LENGTH, max = UserPayload.USERNAME_MAX_LENGTH,
                message = "the length of the username must be between " +
                        UserPayload.USERNAME_MIN_LENGTH + " and " + UserPayload.USERNAME_MAX_LENGTH)
        String username,

        @NotNull(message = "email must be not null")
        @Size(min = UserPayload.EMAIL_MIN_LENGTH, max = UserPayload.EMAIL_MAX_LENGTH,
                message = "the length of the email must be between " +
                        UserPayload.EMAIL_MIN_LENGTH + " and " + UserPayload.EMAIL_MAX_LENGTH)
        @Email(message = "the email is in the wrong format")
        String email,

        @NotNull(message = "password must be not null")
        @Size(min = UserPayload.PASSWORD_MIN_LENGTH, max = UserPayload.USERNAME_MAX_LENGTH,
                message = "the length of the password must be between " +
                        UserPayload.PASSWORD_MIN_LENGTH + " and " + UserPayload.USERNAME_MAX_LENGTH)
        String password,

        @NotNull(message = "User role must be not null")
        User.Role role) {

        public static final int USERNAME_MIN_LENGTH = 5;

        public static final int USERNAME_MAX_LENGTH = 30;

        public static final int EMAIL_MIN_LENGTH = 5;

        public static final int EMAIL_MAX_LENGTH = 50;

        public static final int PASSWORD_MIN_LENGTH = 5;

        public static final int PASSWORD_MAX_LENGTH = 50;

}
