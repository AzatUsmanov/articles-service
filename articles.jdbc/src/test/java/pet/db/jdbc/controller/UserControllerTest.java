package pet.db.jdbc.controller;


import jakarta.servlet.ServletException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import pet.db.jdbc.controller.payload.UserPayload;
import pet.db.jdbc.entity.Article;
import pet.db.jdbc.entity.User;
import pet.db.jdbc.service.ArticleService;
import pet.db.jdbc.tool.producer.AuthenticationDetailsProducer;
import pet.db.jdbc.tool.db.DbCleaner;
import pet.db.jdbc.tool.generator.TestDataGenerator;

import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static pet.db.jdbc.controller.constant.ControllerTestConstants.ErrorMessages.DUPLICATE_FIELD;
import static pet.db.jdbc.controller.constant.ControllerTestConstants.ErrorMessages.VALIDATION_FIELD;
import static pet.db.jdbc.controller.constant.ControllerTestConstants.Fields.EMAIL;
import static pet.db.jdbc.controller.constant.ControllerTestConstants.Fields.USERNAME;
import static pet.db.jdbc.controller.constant.ControllerTestConstants.JsonPaths.ERROR;
import static pet.db.jdbc.controller.constant.ControllerTestConstants.JsonPaths.FIELD;
import static pet.db.jdbc.controller.constant.ControllerTestConstants.JsonPaths.FIELD_ERRORS;
import static pet.db.jdbc.controller.constant.ControllerTestConstants.JsonPaths.ID;
import static pet.db.jdbc.controller.constant.ControllerTestConstants.JsonPaths.LENGTH;
import static pet.db.jdbc.controller.constant.ControllerTestConstants.JsonPaths.PATH;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureJsonTesters
public class UserControllerTest {

    @Value("${api.paths.users}")
    public String USERS_PATH;

    @Value("#{'${api.paths.users}' + '/%d'}")
    public String USERS_ID_PATH;

    @Value("#{'${api.paths.users}' + '/authorship/%d'}")
    public String USERS_AUTHORSHIP_ID_PATH;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DbCleaner dbCleaner;

    @Autowired
    private ArticleService articleService;

    @Autowired
    private JacksonTester<User> userJsonTester;

    @Autowired
    private JacksonTester<List<User>> userListJsonTester;

    @Autowired
    private JacksonTester<UserPayload> UserPayloadJsonTester;

    @Autowired
    private TestDataGenerator<UserPayload> UserPayloadTestDataGenerator;

    @Autowired
    private TestDataGenerator<User> userTestDataGenerator;

    @Autowired
    private TestDataGenerator<Article> articleTestDataGenerator;


    @Autowired
    private Converter<User, UserDetails> userToUserDetailsConverter;

    @Autowired
    private AuthenticationDetailsProducer authenticationDetailsProducer;

    private UserDetails registeredUser;

    private UserDetails registeredAdmin;

    @BeforeEach
    public void initAuthenticationDetails() {
        registeredUser = authenticationDetailsProducer.produceUserDetailsOfRegisteredUser(User.Role.ROLE_USER);
        registeredAdmin = authenticationDetailsProducer.produceUserDetailsOfRegisteredUser(User.Role.ROLE_ADMIN);
    }

    @AfterEach
    public void cleanDb() {
        dbCleaner.cleanAll();
    }

    @Test
    public void createUser() throws Exception {
        UserPayload userPayload = UserPayloadTestDataGenerator.generateUnsavedData();
        var request = post(USERS_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(registeredAdmin))
                .content(UserPayloadJsonTester.write(userPayload).getJson());

        mockMvc.perform(request)
                .andExpectAll(
                        status().isCreated(),
                        jsonPath(ID).isNumber()
                ).andDo(result -> {
                    User user = getUserFromMvcResult(result);
                    assertTrue(isUserMatchesUserPayload(user, userPayload));
                });
    }

    @Test
    public void createUserWithoutAccess() throws Exception {
        UserPayload userPayload = UserPayloadTestDataGenerator.generateUnsavedData();
        var request = post(USERS_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(registeredUser))
                .content(UserPayloadJsonTester.write(userPayload).getJson());

        mockMvc.perform(request)
                .andExpect(status().isForbidden());
    }

    @Test
    public void createUserWithInvalidData() throws Exception {
        UserPayload userPayloadWithInvalidData = new UserPayload("", "", "", User.Role.ROLE_USER);
        var request = post(USERS_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(registeredAdmin))
                .content(UserPayloadJsonTester.write(userPayloadWithInvalidData).getJson());

        mockMvc.perform(request)
                .andExpectAll(
                        status().isUnprocessableEntity(),
                        jsonPath(ERROR).value(VALIDATION_FIELD),
                        jsonPath(FIELD_ERRORS).isString()
                );
    }

    @Test
    public void createUserWithNotUniqueUsername() throws Exception {
        User savedUser = userTestDataGenerator.generateSavedData();
        User unsavedUser = userTestDataGenerator.generateUnsavedData();
        UserPayload userPayload = new UserPayload(
                savedUser.getUsername(),
                unsavedUser.getEmail(),
                unsavedUser.getPassword(),
                unsavedUser.getRole());
        var request = post(USERS_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(registeredAdmin))
                .content(UserPayloadJsonTester.write(userPayload).getJson());


        mockMvc.perform(request)
                .andExpectAll(
                        status().isConflict(),
                        jsonPath(ERROR).value(DUPLICATE_FIELD),
                        jsonPath(FIELD).value(USERNAME)
                );
    }

    @Test
    public void createUserWithNotUniqueEmail() throws Exception {
        User savedUser = userTestDataGenerator.generateSavedData();
        User unsavedUser = userTestDataGenerator.generateUnsavedData();
        UserPayload userPayload = new UserPayload(
                unsavedUser.getUsername(),
                savedUser.getEmail(),
                unsavedUser.getPassword(),
                unsavedUser.getRole());
        var request = post(USERS_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(registeredAdmin))
                .content(UserPayloadJsonTester.write(userPayload).getJson());

        mockMvc.perform(request)
                .andExpectAll(
                        status().isConflict(),
                        jsonPath(ERROR).value(DUPLICATE_FIELD),
                        jsonPath(FIELD).value(EMAIL)
                );
    }

    @Test
    public void updateUserById() throws Exception {
        User savedUser = userTestDataGenerator.generateSavedData();
        UserPayload userPayload = UserPayloadTestDataGenerator.generateUnsavedData();
        var request = patch(USERS_ID_PATH.formatted(savedUser.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(registeredAdmin))
                .content(UserPayloadJsonTester.write(userPayload).getJson());

        mockMvc.perform(request)
                .andExpectAll(
                        status().isOk(),
                        jsonPath(ID).value(savedUser.getId())
                ).andDo(result -> {
                    User user = getUserFromMvcResult(result);
                    assertTrue(isUserMatchesUserPayload(user, userPayload));
                });
    }

    @Test
    public void updateUserByIdViaTargetUser() throws Exception {
        User savedUser = authenticationDetailsProducer.produceRegisteredUserWithRawPassword(User.Role.ROLE_USER);
        UserDetails targetUserDetails = userToUserDetailsConverter.convert(savedUser);
        UserPayload userPayload = UserPayloadTestDataGenerator.generateUnsavedData();
        var request = patch(USERS_ID_PATH.formatted(savedUser.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(Objects.requireNonNull(targetUserDetails)))
                .content(UserPayloadJsonTester.write(userPayload).getJson());

        mockMvc.perform(request)
                .andExpectAll(
                        status().isOk(),
                        jsonPath(ID).value(savedUser.getId())
                ).andDo(result -> {
                    User user = getUserFromMvcResult(result);
                    assertTrue(isUserMatchesUserPayload(user, userPayload));
                });
    }

    @Test
    public void updateUserByIdWithoutAccess() throws Exception {
        User savedUser = userTestDataGenerator.generateSavedData();
        UserPayload userPayload = UserPayloadTestDataGenerator.generateUnsavedData();
        var request = patch(USERS_ID_PATH.formatted(savedUser.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(registeredUser))
                .content(UserPayloadJsonTester.write(userPayload).getJson());

        mockMvc.perform(request)
                .andExpect(status().isForbidden());
    }

    @Test
    public void updateUserByIdWithSameUsernameAndEmail() throws Exception {
        User savedUser = userTestDataGenerator.generateSavedData();
        User userDataForUpdate = userTestDataGenerator.generateUnsavedData();
        UserPayload userPayload = new UserPayload(
                savedUser.getUsername(),
                savedUser.getEmail(),
                userDataForUpdate.getPassword(),
                userDataForUpdate.getRole()
        );
        var request = patch(USERS_ID_PATH.formatted(savedUser.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(registeredAdmin))
                .content(UserPayloadJsonTester.write(userPayload).getJson());

        mockMvc.perform(request)
                .andExpectAll(
                        status().isOk(),
                        jsonPath(ID).value(savedUser.getId())
                ).andDo(result -> {
                    User user = getUserFromMvcResult(result);
                    assertTrue(isUserMatchesUserPayload(user, userPayload));
                });
    }

    @Test
    public void updateUserByNonExistentId() throws Exception {
        User unsavedUser = userTestDataGenerator.generateUnsavedData();
        UserPayload userPayload = UserPayloadTestDataGenerator.generateUnsavedData();
        var request = patch(USERS_ID_PATH.formatted(unsavedUser.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(registeredAdmin))
                .content(UserPayloadJsonTester.write(userPayload).getJson());

        assertThrows(ServletException.class, () -> mockMvc.perform(request));
    }

    @Test
    public void updateUserByIdWithNotUniqueUsername() throws Exception {
        User savedUser = userTestDataGenerator.generateSavedData();
        User anotherSavedUser = userTestDataGenerator.generateSavedData();
        User userDataForUpdate = userTestDataGenerator.generateUnsavedData();
        UserPayload userPayload = new UserPayload(
                anotherSavedUser.getUsername(),
                userDataForUpdate.getEmail(),
                userDataForUpdate.getPassword(),
                userDataForUpdate.getRole());
        var request = patch(USERS_ID_PATH.formatted(savedUser.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(registeredAdmin))
                .content(UserPayloadJsonTester.write(userPayload).getJson());

        mockMvc.perform(request)
                .andExpectAll(
                        status().isConflict(),
                        jsonPath(ERROR).value(DUPLICATE_FIELD),
                        jsonPath(FIELD).value(USERNAME)
                );
    }

    @Test
    public void updateUserByIdWithNotUniqueEmail() throws Exception {
        User savedUser = userTestDataGenerator.generateSavedData();
        User anotherSavedUser = userTestDataGenerator.generateSavedData();
        User userDataForUpdate = userTestDataGenerator.generateUnsavedData();
        UserPayload userPayload = new UserPayload(
                userDataForUpdate.getUsername(),
                anotherSavedUser.getEmail(),
                userDataForUpdate.getPassword(),
                userDataForUpdate.getRole());
        var request = patch(USERS_ID_PATH.formatted(savedUser.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(registeredAdmin))
                .content(UserPayloadJsonTester.write(userPayload).getJson());

        mockMvc.perform(request)
                .andExpectAll(
                        status().isConflict(),
                        jsonPath(ERROR).value(DUPLICATE_FIELD),
                        jsonPath(FIELD).value(EMAIL)
                );
    }

    @Test
    public void deleteUserById() throws Exception {
        User savedUser = userTestDataGenerator.generateSavedData();
        var request = delete(USERS_ID_PATH.formatted(savedUser.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(registeredAdmin));

        mockMvc.perform(request)
                .andExpectAll(status().isNoContent());
    }

    @Test
    public void deleteUserByIdViaTargetUser() throws Exception {
        User savedUser = authenticationDetailsProducer.produceRegisteredUserWithRawPassword(User.Role.ROLE_USER);
        UserDetails targetUserDetails = userToUserDetailsConverter.convert(savedUser);
        var request = delete(USERS_ID_PATH.formatted(savedUser.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(Objects.requireNonNull(targetUserDetails)));

        mockMvc.perform(request)
                .andExpectAll(status().isNoContent());
    }

    @Test
    public void deleteUserByIdWithoutAccess() throws Exception {
        User savedUser = userTestDataGenerator.generateSavedData();
        var request = delete(USERS_ID_PATH.formatted(savedUser.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(registeredUser));

        mockMvc.perform(request)
                .andExpectAll(status().isForbidden());
    }

    @Test
    public void deleteUserByNonExistentId() throws Exception {
        User unsavedUser = userTestDataGenerator.generateUnsavedData();
        var request = delete(USERS_ID_PATH.formatted(unsavedUser.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(registeredAdmin));

        mockMvc.perform(request)
                .andExpectAll(status().isNoContent());
    }

    @Test
    public void findUserById() throws Exception {
        User savedUser = userTestDataGenerator.generateSavedData();
        var request = get(USERS_ID_PATH.formatted(savedUser.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(registeredUser));

        mockMvc.perform(request)
                .andExpectAll(
                        status().isOk(),
                        jsonPath(ID).value(savedUser.getId())
                ).andDo(result -> {
                    User user = getUserFromMvcResult(result);
                    assertEquals(savedUser, user);
                });
    }

    @Test
    public void findUserByNonExistentId() throws Exception {
        User unsavedUser = userTestDataGenerator.generateUnsavedData();
        var request = get(USERS_ID_PATH.formatted(unsavedUser.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(registeredUser));

        mockMvc.perform(request)
                .andExpectAll(status().isNotFound());
    }

    @Test
    public void findAuthorsByArticleId() throws Exception {
        Article unsavedArticle = articleTestDataGenerator.generateUnsavedData();
        List<User> authors = new ArrayList<>(userTestDataGenerator.generateSavedData(10));
        List<Integer> authorIds = authors.stream()
                .map(User::getId)
                .toList();
        Article savedArticle = articleService.create(unsavedArticle, authorIds);
        var request = get(USERS_AUTHORSHIP_ID_PATH.formatted(savedArticle.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(registeredAdmin));

        mockMvc.perform(request)
                .andExpectAll(
                        status().isOk(),
                        jsonPath(PATH).isArray(),
                        jsonPath(LENGTH).value(authors.size())
                ).andDo(result -> {
                    String content = result.getResponse().getContentAsString();
                    List<User> users = userListJsonTester.parseObject(content);
                    assertTrue(authors.containsAll(users));
                    assertTrue(users.containsAll(authors));
                });
    }

    @Test
    public void findAuthorsByNonExistentArticleId() throws Exception {
        Article unsavedArticle = articleTestDataGenerator.generateUnsavedData();
        var request = get(USERS_AUTHORSHIP_ID_PATH.formatted(unsavedArticle.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(Objects.requireNonNull(registeredAdmin)));

        assertThrows(ServletException.class, () -> mockMvc.perform(request));
    }

    @Test
    public void findAllUsers() throws Exception {
        cleanDb();
        User registredUser = authenticationDetailsProducer.produceRegisteredUserWithRawPassword(User.Role.ROLE_USER);
        UserDetails UserDetails = userToUserDetailsConverter.convert(registredUser);
        List<User> allUsers = new ArrayList<>(userTestDataGenerator.generateSavedData(10));
        allUsers.add(registredUser);
        var request = get(USERS_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(Objects.requireNonNull(UserDetails)));

        mockMvc.perform(request)
                .andExpectAll(
                        status().isOk(),
                        jsonPath(PATH).isArray(),
                        jsonPath(LENGTH).value(allUsers.size())
                ).andDo(result -> {
                    String content = result.getResponse().getContentAsString();
                    List<User> responseUsers = userListJsonTester.parseObject(content);
                    assertTrue(allUsers.containsAll(responseUsers));
                    assertTrue(responseUsers.containsAll(allUsers));
                });
    }

    private boolean isUserMatchesUserPayload(User user, UserPayload userPayload) {
        return Objects.equals(user.getUsername(), userPayload.username()) &&
                Objects.equals(user.getEmail(), userPayload.email()) &&
                Objects.equals(user.getRole(), userPayload.role());
    }

    private User getUserFromMvcResult(MvcResult mvcResult) throws Exception {
        String content = mvcResult.getResponse().getContentAsString();
        return userJsonTester.parseObject(content);
    }

}
