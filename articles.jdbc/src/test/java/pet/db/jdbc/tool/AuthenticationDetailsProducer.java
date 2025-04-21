package pet.db.jdbc.tool;

import org.springframework.security.core.userdetails.UserDetails;
import pet.db.jdbc.entity.User;

public interface AuthenticationDetailsProducer {
    UserDetails produceUserDetailsOfRegisteredUser(User.Role role);

    User produceRegisteredUserWithRawPassword(User.Role role);
}
