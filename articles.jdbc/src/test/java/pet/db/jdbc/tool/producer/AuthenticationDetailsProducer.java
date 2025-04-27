package pet.db.jdbc.tool.producer;

import jakarta.validation.constraints.NotNull;

import org.springframework.security.core.userdetails.UserDetails;

import pet.db.jdbc.model.dto.User;
import pet.db.jdbc.model.enums.UserRole;

public interface AuthenticationDetailsProducer {

    UserDetails produceUserDetailsOfRegisteredUser(@NotNull UserRole role);

    User produceRegisteredUserWithRawPassword(@NotNull UserRole role);

}
