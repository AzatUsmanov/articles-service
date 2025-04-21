package pet.db.jdbc.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import pet.db.jdbc.controller.payload.RegistrationPayload;
import pet.db.jdbc.controller.payload.UserPayload;
import pet.db.jdbc.entity.User;
import pet.db.jdbc.tool.db.DbCleaner;
import pet.db.jdbc.tool.generator.TestDataGenerator;

import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureJsonTesters
public class RegistrationControllerTest {

    public static final String REGISTRATION_PATH = "/api/registration";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DbCleaner dbCleaner;

    @Autowired
    private JacksonTester<User> userJsonTester;

    @Autowired
    private JacksonTester<RegistrationPayload> registrationPayloadJsonTester;
    
    @Autowired
    private TestDataGenerator<RegistrationPayload> registrationPayloadTestDataGenerator;

    @Autowired
    private TestDataGenerator<User> userTestDataGenerator;

    @AfterEach
    public void cleanDB() {
        dbCleaner.cleanAll();
    }

    @Test
    public void registerUser() throws Exception {
        RegistrationPayload registrationPayload = registrationPayloadTestDataGenerator.generateUnsavedData();
        var request = post(REGISTRATION_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(registrationPayloadJsonTester.write(registrationPayload).getJson());

        mockMvc.perform(request)
                .andExpectAll(
                        status().isCreated(),
                        jsonPath("$.id").isNumber()
                ).andDo(result -> {
                    User user = getUserFromMvcResult(result);
                    assertTrue(isUserMatchesRegistrationPayload(user, registrationPayload));
                });
    }

    @Test
    public void registerWithInvalidData() throws Exception {
        RegistrationPayload registrationPayload = new RegistrationPayload("", "", "");
        var request = post(REGISTRATION_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(registrationPayloadJsonTester.write(registrationPayload).getJson());

        mockMvc.perform(request)
                .andExpectAll(
                        status().isUnprocessableEntity(),
                        jsonPath("$.error").value("validation_field"),
                        jsonPath("$.field_errors").isString()
                );
    }

    @Test
    public void registerUserWithNotUniqueUsername() throws Exception {
        User savedUser = userTestDataGenerator.generateSavedData();
        User unsavedUser = userTestDataGenerator.generateUnsavedData();
        RegistrationPayload registrationPayload = new RegistrationPayload(
          savedUser.getUsername(),
          unsavedUser.getEmail(),
          unsavedUser.getPassword()
        );
        var request = post(REGISTRATION_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(registrationPayloadJsonTester.write(registrationPayload).getJson());

        mockMvc.perform(request)
                .andExpectAll(
                        status().isConflict(),
                        jsonPath("$.error").value("duplicate_field"),
                        jsonPath("$.field").value("username")
                );
    }

    @Test
    public void registerUserWithNotUniqueEmail() throws Exception {
        User savedUser = userTestDataGenerator.generateSavedData();
        User unsavedUser = userTestDataGenerator.generateUnsavedData();
        RegistrationPayload registrationPayload = new RegistrationPayload(
                unsavedUser.getUsername(),
                savedUser.getEmail(),
                unsavedUser.getPassword()
        );
        var request = post(REGISTRATION_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(registrationPayloadJsonTester.write(registrationPayload).getJson());


        mockMvc.perform(request)
                .andExpectAll(
                        status().isConflict(),
                        jsonPath("$.error").value("duplicate_field"),
                        jsonPath("$.field").value("email")
                );
    }

    private boolean isUserMatchesRegistrationPayload(User user, RegistrationPayload registrationPayload) {
        return Objects.equals(user.getUsername(), registrationPayload.username()) &&
                Objects.equals(user.getEmail(), registrationPayload.email());
    }

    private User getUserFromMvcResult(MvcResult mvcResult) throws Exception {
        String content = mvcResult.getResponse().getContentAsString();
        return userJsonTester.parseObject(content);
    }

}
