package pet.db.jdbc.controller;


import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
import pet.db.jdbc.service.RegistrationService;
import pet.db.jdbc.service.UserService;
import pet.db.jdbc.tool.AuthenticationDetailsProducer;
import pet.db.jdbc.tool.db.DbCleaner;
import pet.db.jdbc.tool.generator.TestDataGenerator;
import org.springframework.security.core.userdetails.UserDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureJsonTesters
public class UserControllerTest {

    public static class UserTestConstants {
        public static final String USERS_PATH = "/api/users";
        public static final String USERS_PATH_ID = "/api/users/%d";
        public static final String USERS_AUTHORSHIP_ID_PATH = "/api/users/authorship/%d";

        public static class Fields {
            public static final String USERNAME = "username";
            public static final String EMAIL = "email";
            public static final String PASSWORD = "password";
            public static final String ROLE = "role";
        }

        public static class JsonPaths {
            public static final String ID = "$.id";
            public static final String USERNAME = "$.username";
            public static final String EMAIL = "$.email";
            public static final String ROLE = "$.role";
        }
    }

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
    private AuthenticationDetailsProducer authenticationDetailsProducer;

    @Autowired
    private Converter<User, UserDetails> userToUserDetailsConverter;

    @Autowired
    private AuthenticationDetailsProducer authenticationDataGenerator;

    private UserDetails registeredUser;

    private UserDetails registeredAdmin;

    @BeforeEach
    public void initAuthenticationDetails() {
        registeredUser = authenticationDataGenerator.produceUserDetailsOfRegisteredUser(User.Role.ROLE_USER);
        registeredAdmin = authenticationDataGenerator.produceUserDetailsOfRegisteredUser(User.Role.ROLE_ADMIN);
    }

    @AfterEach
    public void cleanDb() {
        dbCleaner.cleanAll();
    }

    @Test
    public void createUser() throws Exception {
        UserPayload userPayload = UserPayloadTestDataGenerator.generateUnsavedData();
        var request = post(UserTestConstants.USERS_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(registeredAdmin))
                .content(UserPayloadJsonTester.write(userPayload).getJson());

        mockMvc.perform(request)
                .andExpectAll(
                        status().isCreated(),
                        jsonPath(UserTestConstants.JsonPaths.ID).isNumber()
                ).andDo(result -> {
                    User user = getUserFromMvcResult(result);
                    assertTrue(isUserMatchesUserPayload(user, userPayload));
                });
    }

    @Test
    public void createUserWithoutAccess() throws Exception {
        UserPayload userPayload = UserPayloadTestDataGenerator.generateUnsavedData();
        var request = post(UserTestConstants.USERS_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(registeredUser))
                .content(UserPayloadJsonTester.write(userPayload).getJson());

        mockMvc.perform(request)
                .andExpect(status().isForbidden());
    }

    @Test
    public void createUserWithInvalidData() throws Exception {
        UserPayload userPayloadWithInvalidData = new UserPayload("", "", "", User.Role.ROLE_USER);
        var request = post(UserTestConstants.USERS_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(registeredAdmin))
                .content(UserPayloadJsonTester.write(userPayloadWithInvalidData).getJson());

        mockMvc.perform(request)
                .andExpectAll(
                        status().isUnprocessableEntity(),
                        jsonPath("$.error").value("validation_field"),
                        jsonPath("$.field_errors").isString()
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
        var request = post(UserTestConstants.USERS_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(registeredAdmin))
                .content(UserPayloadJsonTester.write(userPayload).getJson());


        mockMvc.perform(request)
                .andExpectAll(
                        status().isConflict(),
                        jsonPath("$.error").value("duplicate_field"),
                        jsonPath("$.field").value(UserTestConstants.Fields.USERNAME)
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
        var request = post(UserTestConstants.USERS_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(registeredAdmin))
                .content(UserPayloadJsonTester.write(userPayload).getJson());

        mockMvc.perform(request)
                .andExpectAll(
                        status().isConflict(),
                        jsonPath("$.error").value("duplicate_field"),
                        jsonPath("$.field").value(UserTestConstants.Fields.EMAIL)
                );
    }

    @Test
    public void updateUserById() throws Exception {
        User savedUser = userTestDataGenerator.generateSavedData();
        UserPayload userPayload = UserPayloadTestDataGenerator.generateUnsavedData();
        var request = patch(String.format(UserTestConstants.USERS_PATH_ID, savedUser.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(registeredAdmin))
                .content(UserPayloadJsonTester.write(userPayload).getJson());

        mockMvc.perform(request)
                .andExpectAll(
                        status().isOk(),
                        jsonPath(UserTestConstants.JsonPaths.ID).value(savedUser.getId())
                ).andDo(result -> {
                    User user = getUserFromMvcResult(result);
                    assertTrue(isUserMatchesUserPayload(user, userPayload));
                });
    }

    @Test
    public void updateUserByIdViaTargetUser() throws Exception {
        User savedUser = authenticationDataGenerator.produceRegisteredUserWithRawPassword(User.Role.ROLE_USER);
        UserDetails targetUserDetails = userToUserDetailsConverter.convert(savedUser);
        UserPayload userPayload = UserPayloadTestDataGenerator.generateUnsavedData();
        var request = patch(String.format(UserTestConstants.USERS_PATH_ID, savedUser.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(Objects.requireNonNull(targetUserDetails)))
                .content(UserPayloadJsonTester.write(userPayload).getJson());

        mockMvc.perform(request)
                .andExpectAll(
                        status().isOk(),
                        jsonPath(UserTestConstants.JsonPaths.ID).value(savedUser.getId())
                ).andDo(result -> {
                    User user = getUserFromMvcResult(result);
                    assertTrue(isUserMatchesUserPayload(user, userPayload));
                });
    }

    @Test
    public void updateUserByIdWithoutAccess() throws Exception {
        User savedUser = userTestDataGenerator.generateSavedData();
        UserPayload userPayload = UserPayloadTestDataGenerator.generateUnsavedData();
        var request = patch(String.format(UserTestConstants.USERS_PATH_ID, savedUser.getId()))
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
        var request = patch(String.format(UserTestConstants.USERS_PATH_ID, savedUser.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(registeredAdmin))
                .content(UserPayloadJsonTester.write(userPayload).getJson());

        mockMvc.perform(request)
                .andExpectAll(
                        status().isOk(),
                        jsonPath(UserTestConstants.JsonPaths.ID).value(savedUser.getId())
                ).andDo(result -> {
                    User user = getUserFromMvcResult(result);
                    assertTrue(isUserMatchesUserPayload(user, userPayload));
                });
    }

    @Test
    public void updateUserByNonExistentId() throws Exception {
        User unsavedUser = userTestDataGenerator.generateUnsavedData();
        UserPayload userPayload = UserPayloadTestDataGenerator.generateUnsavedData();
        var request = patch(String.format(UserTestConstants.USERS_PATH_ID, unsavedUser.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(registeredAdmin))
                .content(UserPayloadJsonTester.write(userPayload).getJson());

        assertThatThrownBy(() -> mockMvc.perform(request))
                .isInstanceOf(ServletException.class);
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
        var request = patch(String.format(UserTestConstants.USERS_PATH_ID, savedUser.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(registeredAdmin))
                .content(UserPayloadJsonTester.write(userPayload).getJson());

        mockMvc.perform(request)
                .andExpectAll(
                        status().isConflict(),
                        jsonPath("$.error").value("duplicate_field"),
                        jsonPath("$.field").value(UserTestConstants.Fields.USERNAME)
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
        var request = patch(String.format(UserTestConstants.USERS_PATH_ID, savedUser.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(registeredAdmin))
                .content(UserPayloadJsonTester.write(userPayload).getJson());

        mockMvc.perform(request)
                .andExpectAll(
                        status().isConflict(),
                        jsonPath("$.error").value("duplicate_field"),
                        jsonPath("$.field").value(UserTestConstants.Fields.EMAIL)
                );
    }

    @Test
    public void deleteUserById() throws Exception {
        User savedUser = userTestDataGenerator.generateSavedData();
        var request = delete(String.format(UserTestConstants.USERS_PATH_ID, savedUser.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(registeredAdmin));

        mockMvc.perform(request)
                .andExpectAll(status().isNoContent());
    }

    @Test
    public void deleteUserByIdViaTargetUser() throws Exception {
        User savedUser = authenticationDataGenerator.produceRegisteredUserWithRawPassword(User.Role.ROLE_USER);
        UserDetails targetUserDetails = userToUserDetailsConverter.convert(savedUser);
        var request = delete(String.format(UserTestConstants.USERS_PATH_ID, savedUser.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(Objects.requireNonNull(targetUserDetails)));

        mockMvc.perform(request)
                .andExpectAll(status().isNoContent());
    }

    @Test
    public void deleteUserByIdWithoutAccess() throws Exception {
        User savedUser = userTestDataGenerator.generateSavedData();
        var request = delete(String.format(UserTestConstants.USERS_PATH_ID, savedUser.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(registeredUser));

        mockMvc.perform(request)
                .andExpectAll(status().isForbidden());
    }

    @Test
    public void deleteUserByNonExistentId() throws Exception {
        User unsavedUser = userTestDataGenerator.generateUnsavedData();
        var request = delete(String.format(UserTestConstants.USERS_PATH_ID, unsavedUser.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(registeredAdmin));

        mockMvc.perform(request)
                .andExpectAll(status().isNoContent());
    }

    @Test
    public void findUserById() throws Exception {
        User savedUser = userTestDataGenerator.generateSavedData();
        var request = get(String.format(UserTestConstants.USERS_PATH_ID, savedUser.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(registeredUser));

        mockMvc.perform(request)
                .andExpectAll(
                        status().isOk(),
                        jsonPath(UserTestConstants.JsonPaths.ID).value(savedUser.getId())
                ).andDo(result -> {
                    User user = getUserFromMvcResult(result);
                    assertEquals(savedUser, user);
                });
    }

    @Test
    public void findUserByNonExistentId() throws Exception {
        User unsavedUser = userTestDataGenerator.generateUnsavedData();
        var request = get(String.format(UserTestConstants.USERS_PATH_ID, unsavedUser.getId()))
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
        var request = get(String.format(UserTestConstants.USERS_AUTHORSHIP_ID_PATH, savedArticle.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(registeredAdmin));

        mockMvc.perform(request)
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$").isArray(),
                        jsonPath("$.length()").value(authors.size())
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
        var request = get(String.format(UserTestConstants.USERS_AUTHORSHIP_ID_PATH, unsavedArticle.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(Objects.requireNonNull(registeredAdmin)));

        assertThatThrownBy(() -> mockMvc.perform(request))
                .isInstanceOf(ServletException.class);
    }

    @Test
    public void findAllUsers() throws Exception {
        cleanDb();
        User registredUser = authenticationDataGenerator.produceRegisteredUserWithRawPassword(User.Role.ROLE_USER);
        UserDetails UserDetails = userToUserDetailsConverter.convert(registredUser);
        List<User> allUsers = new ArrayList<>(userTestDataGenerator.generateSavedData(10));
        allUsers.add(registredUser);
        var request = get(UserTestConstants.USERS_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(Objects.requireNonNull(UserDetails)));

        mockMvc.perform(request)
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$").isArray(),
                        jsonPath("$.length()").value(allUsers.size())
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
