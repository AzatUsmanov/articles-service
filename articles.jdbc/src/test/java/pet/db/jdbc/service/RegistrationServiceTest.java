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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
        User userForRegistration = userTestDataGenerator.generateUnsavedData();
        userForRegistration.setUsername(null);

        assertThrows(RuntimeException.class, () -> registrationService.register(userForRegistration));
    }

    @Test
    public void registerUserWithNotUniqueUsername() {
        User savedUser = userTestDataGenerator.generateSavedData();
        User userForRegistration = userTestDataGenerator.generateUnsavedData();
        userForRegistration.setUsername(savedUser.getUsername());

        assertThrows(DuplicateUserException.class, () -> registrationService.register(userForRegistration));
    }

    @Test
    public void registerUserWithNotUniqueEmail() {
        User savedUser = userTestDataGenerator.generateSavedData();
        User userForRegistration = userTestDataGenerator.generateUnsavedData();
        userForRegistration.setEmail(savedUser.getEmail());

        assertThrows(DuplicateUserException.class, () -> registrationService.register(userForRegistration));
    }

}
