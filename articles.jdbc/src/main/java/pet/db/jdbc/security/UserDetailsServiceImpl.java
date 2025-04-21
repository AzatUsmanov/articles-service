package pet.db.jdbc.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import pet.db.jdbc.entity.Article;
import pet.db.jdbc.repository.UserRepository;
import pet.db.jdbc.service.UserService;
import pet.db.jdbc.tool.converter.UserToUserDetailsConverter;

import java.sql.SQLException;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserService userService;

    private final UserToUserDetailsConverter userToUserDetailsConverter;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userService
                .findByUsername(username)
                .map(userToUserDetailsConverter::convert)
                .orElseThrow(() -> new UsernameNotFoundException("User with name \"" + username + "\" not found"));
    }

}
