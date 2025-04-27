package pet.db.jdbc.tool.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.stereotype.Component;

import pet.db.jdbc.model.dto.User;

import java.util.List;

@Component
public class UserToUserDetailsConverter implements Converter<User, UserDetails> {

    @Override
    public UserDetails convert(User source) {
        return new org.springframework.security.core.userdetails.User(
                source.getUsername(),
                source.getPassword(),
                List.of(new SimpleGrantedAuthority(source.getRole().toString()))
        );
    }

}
