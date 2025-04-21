package pet.db.jdbc.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import pet.db.jdbc.entity.User;
import pet.db.jdbc.tool.db.DbCleaner;
import pet.db.jdbc.tool.generator.TestDataGenerator;
import pet.db.jdbc.tool.exception.DuplicateUserException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class RegistrationServiceTest {

    @Autowired
    private DbCleaner dbCleaner;

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TestDataGenerator<User> userTestDataGenerator;

    @AfterEach
    void cleanDB() {
        dbCleaner.cleanAll();
    }

    @Test
    public void registerUser() throws DuplicateUserException {
        User userForRegistration = userTestDataGenerator.generateUnsavedData();
        String userForRegistrationRawPassword = userForRegistration.getPassword();
        User registeredUser = registrationService.register(userForRegistration);

        Optional<User> userOptionalForCheck = userService.findById(registeredUser.getId());
        assertTrue(userOptionalForCheck.isPresent());
        assertEquals(registeredUser, userOptionalForCheck.get());
        assertTrue(passwordEncoder.matches(
                        userForRegistrationRawPassword,
                        userOptionalForCheck.get().getPassword()));
    }

    @Test
    public void registerWithInvalidData() {
        User userForSave = userTestDataGenerator.generateUnsavedData();
        userForSave.setUsername(null);

        assertThatThrownBy(() -> userService.create(userForSave))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    public void registerUserWithNotUniqueUsername() {
        User savedUser = userTestDataGenerator.generateSavedData();
        User unsavedUser = userTestDataGenerator.generateUnsavedData();
        unsavedUser.setUsername(savedUser.getUsername());

        assertThatThrownBy(() -> userService.create(unsavedUser))
                .isInstanceOf(DuplicateUserException.class);
    }

    @Test
    public void registerUserWithNotUniqueEmail() {
        User savedUser = userTestDataGenerator.generateSavedData();
        User unsavedUser = userTestDataGenerator.generateUnsavedData();
        unsavedUser.setEmail(savedUser.getEmail());

        assertThatThrownBy(() -> userService.create(unsavedUser))
                .isInstanceOf(DuplicateUserException.class);
    }


}
