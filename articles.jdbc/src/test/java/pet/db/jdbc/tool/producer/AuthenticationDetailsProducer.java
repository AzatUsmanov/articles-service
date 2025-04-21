package pet.db.jdbc.tool.producer;

import jakarta.validation.constraints.NotNull;

import org.springframework.security.core.userdetails.UserDetails;

import pet.db.jdbc.entity.User;

public interface AuthenticationDetailsProducer {

    UserDetails produceUserDetailsOfRegisteredUser(@NotNull User.Role role);

    User produceRegisteredUserWithRawPassword(@NotNull User.Role role);

}
