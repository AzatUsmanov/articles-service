package pet.db.jdbc.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import pet.db.jdbc.controller.payload.UserPayload;
import pet.db.jdbc.controller.payload.RegistrationPayload;

import java.io.Serializable;
import java.util.Arrays;
import java.util.NoSuchElementException;


@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class User {


    @Getter
    public enum Role {
        ROLE_USER(0), ROLE_ADMIN(1);

        private final int number;

        Role(int number) {
            this.number = number;
        }

        public static User.Role getByNumber(Integer number) {
            return Arrays
                    .stream(User.Role.values())
                    .filter(x -> x.getNumber() == number).
                    findFirst()
                    .orElseThrow(NoSuchElementException::new);
        }

    }

    private Integer id;

    private String username;

    private String email;

    private Role role;

    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private String password;

    public User(RegistrationPayload registrationPayload) {
        this.username = registrationPayload.username();
        this.email = registrationPayload.email();
        this.password = registrationPayload.password();
        this.role = Role.ROLE_USER;
    }

    public User(UserPayload userPayload) {
        this.username = userPayload.username();
        this.email = userPayload.email();
        this.password = userPayload.password();
        this.role = userPayload.role();
    }

}
