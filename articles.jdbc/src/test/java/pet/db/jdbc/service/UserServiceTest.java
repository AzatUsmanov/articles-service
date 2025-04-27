package pet.db.jdbc.service;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import pet.db.jdbc.model.dto.Article;
import pet.db.jdbc.model.dto.User;
import pet.db.jdbc.tool.db.DbCleaner;
import pet.db.jdbc.tool.generator.TestDataGenerator;
import pet.db.jdbc.tool.exception.DuplicateUserException;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class UserServiceTest {

    @Autowired
    private DbCleaner dbCleaner;

    @Autowired
    private UserService userService;

    @Autowired
    private TestDataGenerator<User> userTestDataGenerator;

    @Autowired
    private TestDataGenerator<Article> articleTestDataGenerator;

    @AfterEach
    void cleanDb() {
        dbCleaner.cleanAll();
    }

    @Test
    public void saveUser() throws DuplicateUserException {
        User userForSave = userTestDataGenerator.generateUnsavedData();

        User savedUser = userService.create(userForSave);

        Optional<User> userOptionalForCheck = userService.findById(savedUser.getId());
        assertTrue(userOptionalForCheck.isPresent());
        assertEquals(savedUser, userOptionalForCheck.get());
    }

    @Test
    public void saveUserWithInvalidData() {
        User userForSave = userTestDataGenerator.generateUnsavedData();
        userForSave.setUsername(null);

        assertThrows(RuntimeException.class, () -> userService.create(userForSave));
    }

    @Test
    public void saveUserWithNotUniqueUsername() {
        User savedUser = userTestDataGenerator.generateSavedData();
        User unsavedUser = userTestDataGenerator.generateUnsavedData();
        unsavedUser.setUsername(savedUser.getUsername());

        assertThrows(DuplicateUserException.class, () -> userService.create(unsavedUser));
    }

    @Test
    public void saveUserWithNotUniqueEmail() {
        User savedUser = userTestDataGenerator.generateSavedData();
        User unsavedUser = userTestDataGenerator.generateUnsavedData();
        unsavedUser.setEmail(savedUser.getEmail());

        assertThrows(DuplicateUserException.class, () -> userService.create(unsavedUser));
    }

    @Test
    public void updateUserById() throws DuplicateUserException {
        User savedUser = userTestDataGenerator.generateSavedData();
        User userDataForUpdate = userTestDataGenerator.generateUnsavedData();

        User updatedUser = userService.updateById(userDataForUpdate, savedUser.getId());

        Optional<User> userOptionalForCheck = userService.findById(savedUser.getId());
        assertTrue(userOptionalForCheck.isPresent());
        assertEquals(updatedUser, userOptionalForCheck.get());
    }

    @Test
    public void updateUserByIdWithSameUsernameAndEmail() throws DuplicateUserException {
        User savedUser = userTestDataGenerator.generateSavedData();
        User userDataForUpdate = userTestDataGenerator.generateUnsavedData();
        userDataForUpdate.setUsername(savedUser.getUsername());
        userDataForUpdate.setEmail(savedUser.getEmail());

        User updatedUser = userService.updateById(userDataForUpdate, savedUser.getId());

        Optional<User> userOptionalForCheck = userService.findById(savedUser.getId());
        assertTrue(userOptionalForCheck.isPresent());
        assertEquals(updatedUser, userOptionalForCheck.get());
    }

    @Test
    public void updateUserByNonExistentId() {
        User unsavedUser = userTestDataGenerator.generateUnsavedData();
        User userDataForUpdate = userTestDataGenerator.generateUnsavedData();

        assertThrows(NoSuchElementException.class,
                () -> userService.updateById(userDataForUpdate, unsavedUser.getId()));
    }

    @Test
    public void updateUserByIdWithNotUniqueUsername() {
        User savedUser = userTestDataGenerator.generateSavedData();
        User userDataForUpdate = userTestDataGenerator.generateUnsavedData();
        User anotherSavedUser = userTestDataGenerator.generateSavedData();
        userDataForUpdate.setUsername(anotherSavedUser.getUsername());

        assertThrows(DuplicateUserException.class,
                () -> userService.updateById(userDataForUpdate, savedUser.getId()));
    }

    @Test
    public void updateUserByIdWithNotUniqueEmail() {
        User savedUser = userTestDataGenerator.generateSavedData();
        User newUserDataForUpdate = userTestDataGenerator.generateUnsavedData();
        User anotherSavedUser = userTestDataGenerator.generateSavedData();
        newUserDataForUpdate.setEmail(anotherSavedUser.getEmail());

        assertThrows(DuplicateUserException.class,
                () -> userService.updateById(newUserDataForUpdate, savedUser.getId()));
    }

    @Test
    public void deleteUserById()  {
        User savedUser = userTestDataGenerator.generateSavedData();

        userService.deleteById(savedUser.getId());

        assertFalse(userService.existsById(savedUser.getId()));
    }

    @Test
    public void deleteUserByNonExistentId()  {
        User unsavedUser = userTestDataGenerator.generateUnsavedData();

        userService.deleteById(unsavedUser.getId());

        assertFalse(userService.existsById(unsavedUser.getId()));
    }

    @Test
    public void findUserById() {
        User savedUser = userTestDataGenerator.generateSavedData();

        Optional<User> userOptionalForCheck = userService.findById(savedUser.getId());

        assertTrue(userOptionalForCheck.isPresent());
        assertEquals(savedUser, userOptionalForCheck.get());
    }

    @Test
    public void findUserByNonExistentId() {
        User unsavedUser = userTestDataGenerator.generateUnsavedData();

        Optional<User> userOptionalForCheck = userService.findById(unsavedUser.getId());

        assertTrue(userOptionalForCheck.isEmpty());
    }

    @Test
    public void findUserByUsername() {
        User savedUser = userTestDataGenerator.generateSavedData();

        Optional<User> userOptionalForCheck = userService.findByUsername(savedUser.getUsername());

        assertTrue(userOptionalForCheck.isPresent());
        assertEquals(savedUser, userOptionalForCheck.get());
    }

    @Test
    public void findUserByNonExistentUsername() {
        User unsavedUser = userTestDataGenerator.generateUnsavedData();

        Optional<User> userOptionalForCheck = userService.findByUsername(unsavedUser.getUsername());

        assertTrue(userOptionalForCheck.isEmpty());
    }

    @Test
    public void findAuthorsByArticleId() {
        Article savedArticle = articleTestDataGenerator.generateSavedData();
        List<User> authors = userService.findAll();

        List<User> authorsForCheck = userService.findAuthorsByArticleId(savedArticle.getId());

        assertEquals(authors.size(), authorsForCheck.size());
        assertTrue(authors.containsAll(authorsForCheck));
        assertTrue(authorsForCheck.containsAll(authors));
    }

    @Test
    public void findAuthorsByNonExistentArticleId() {
        Article unsavedArticle = articleTestDataGenerator.generateUnsavedData();

        assertThrows(NoSuchElementException.class,
                () -> userService.findAuthorsByArticleId(unsavedArticle.getId()));
    }

    @Test
    public void findAllUsers() {
        List<User> allUsers = userTestDataGenerator.generateSavedData(10);

        List<User> usersForCheck = userService.findAll();

        assertEquals(allUsers.size(), usersForCheck.size());
        assertTrue(allUsers.containsAll(usersForCheck));
        assertTrue(usersForCheck.containsAll(allUsers));
    }

}
