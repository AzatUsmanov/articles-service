package pet.db.jdbc.service;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Service;

import pet.db.jdbc.model.dto.User;
import pet.db.jdbc.tool.exception.DuplicateUserException;

@Service
@RequiredArgsConstructor
public class RegistrationServiceImpl implements RegistrationService {

    private final UserService userService;

    private final PasswordEncoder passwordEncoder;

    @Override
    public User register(User user) throws DuplicateUserException {
        User UserForRegistration = buildUserForRegistration(user);
        return userService.create(UserForRegistration);
    }

    private User buildUserForRegistration(User user) {
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        return user.toBuilder()
                .password(encodedPassword)
                .build();
    }

}
