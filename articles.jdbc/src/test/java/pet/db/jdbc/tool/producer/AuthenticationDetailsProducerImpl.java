package pet.db.jdbc.tool.producer;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import pet.db.jdbc.entity.User;
import pet.db.jdbc.service.RegistrationService;
import pet.db.jdbc.tool.exception.DuplicateUserException;
import pet.db.jdbc.tool.generator.TestDataGenerator;

@Component
@RequiredArgsConstructor
public class AuthenticationDetailsProducerImpl implements AuthenticationDetailsProducer {

    @Autowired
    private Converter<User, UserDetails> userToUserDetailsConverter;

    @Autowired
    private TestDataGenerator<User> userTestDataGenerator;

    @Autowired
    private RegistrationService registrationService;

    @Override
    public UserDetails produceUserDetailsOfRegisteredUser(User.Role role) {
        User registredUser = produceRegisteredUserWithRawPassword(role);
        return userToUserDetailsConverter.convert(registredUser);
    }

    @Override
    public User produceRegisteredUserWithRawPassword(User.Role role) {
        User unsavedUser = userTestDataGenerator.generateUnsavedData();
        unsavedUser.setRole(role);
        try {
            return registrationService.register(unsavedUser).toBuilder()
                    .password(unsavedUser.getPassword())
                    .build();
        } catch (DuplicateUserException e) {
            throw new RuntimeException(e);
        }
    }

}
