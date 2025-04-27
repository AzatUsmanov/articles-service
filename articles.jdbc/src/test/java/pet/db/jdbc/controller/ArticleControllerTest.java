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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import pet.db.jdbc.model.dto.payload.NewArticlePayload;
import pet.db.jdbc.model.dto.payload.UpdateArticlePayload;
import pet.db.jdbc.model.dto.Article;
import pet.db.jdbc.model.dto.User;
import pet.db.jdbc.model.enums.UserRole;
import pet.db.jdbc.service.ArticleService;
import pet.db.jdbc.service.UserService;
import pet.db.jdbc.tool.producer.AuthenticationDetailsProducer;
import pet.db.jdbc.tool.db.DbCleaner;
import pet.db.jdbc.tool.generator.TestDataGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static pet.db.jdbc.controller.constant.ControllerTestConstants.ErrorMessages.VALIDATION_FIELD;
import static pet.db.jdbc.controller.constant.ControllerTestConstants.JsonPaths.DATE_OF_CREATION;
import static pet.db.jdbc.controller.constant.ControllerTestConstants.JsonPaths.ERROR;
import static pet.db.jdbc.controller.constant.ControllerTestConstants.JsonPaths.FIELD_ERRORS;
import static pet.db.jdbc.controller.constant.ControllerTestConstants.JsonPaths.ID;
import static pet.db.jdbc.controller.constant.ControllerTestConstants.JsonPaths.LENGTH;
import static pet.db.jdbc.controller.constant.ControllerTestConstants.JsonPaths.PATH;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureJsonTesters
public class ArticleControllerTest {

    @Value("${api.paths.articles}")
    public String ARTICLES_PATH = "/api/articles";

    @Value("#{'${api.paths.articles}' + '/%d'}")
    public String ARTICLES_ID_PATH;

    @Value("#{'${api.paths.articles}' + '/authorship/%d'}")
    public String ARTICLES_AUTHORSHIP_ID_PATH;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DbCleaner dbCleaner;

    @Autowired
    private ArticleService articleService;

    @Autowired
    private UserService userService;

    @Autowired
    private JacksonTester<Article> articleJsonTester;

    @Autowired
    private JacksonTester<List<Article>> articleListJsonTester;

    @Autowired
    private JacksonTester<NewArticlePayload> newArticlePayloadJsonTester;

    @Autowired
    private JacksonTester<UpdateArticlePayload> updateArticlePayloadJsonTester;

    @Autowired
    private TestDataGenerator<Article> articleTestDataGenerator;

    @Autowired
    private TestDataGenerator<NewArticlePayload> newArticlePayloadTestDataGenerator;

    @Autowired
    private TestDataGenerator<UpdateArticlePayload> updateArticlePayloadTestDataGenerator;

    @Autowired
    private TestDataGenerator<User> userTestDataGenerator;

    @Autowired
    private Converter<User, UserDetails> userToUserDetailsConverter;

    @Autowired
    private AuthenticationDetailsProducer authenticationDataGenerator;

    private UserDetails registeredUser;

    private UserDetails registeredAdmin;

    @BeforeEach
    void initAuthenticationData() {
        registeredUser = authenticationDataGenerator.produceUserDetailsOfRegisteredUser(UserRole.ROLE_USER);
        registeredAdmin = authenticationDataGenerator.produceUserDetailsOfRegisteredUser(UserRole.ROLE_ADMIN);
    }

    @AfterEach
    void cleanDB() {
        dbCleaner.cleanAll();
    }


    @Test
    public void createArticle() throws Exception {
        NewArticlePayload newArticlePayload = newArticlePayloadTestDataGenerator.generateUnsavedData();
        var request = post(ARTICLES_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(registeredAdmin))
                .content(newArticlePayloadJsonTester.write(newArticlePayload).getJson());

        mockMvc.perform(request)
                .andExpectAll(
                        status().isCreated(),
                        jsonPath(ID).isNumber(),
                        jsonPath(DATE_OF_CREATION).exists()
                ).andDo(result -> {
                    Article article = getArticleFromMvcResult(result);
                    assertTrue(isArticleMatchesNewArticlePayload(article, newArticlePayload));
                });
    }

    @Test
    public void createArticleViaTargetUser() throws Exception {
        NewArticlePayload newArticlePayload = newArticlePayloadTestDataGenerator.generateUnsavedData();
        User registeredUser = authenticationDataGenerator.produceRegisteredUserWithRawPassword(UserRole.ROLE_USER);
        UserDetails userDetailsOfRegisteredUser = userToUserDetailsConverter.convert(registeredUser);
        newArticlePayload.authorIds().add(registeredUser.getId());
        var request = post(ARTICLES_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(Objects.requireNonNull(userDetailsOfRegisteredUser)))
                .content(newArticlePayloadJsonTester.write(newArticlePayload).getJson());

        mockMvc.perform(request)
                .andExpectAll(
                        status().isCreated(),
                        jsonPath(ID).isNumber(),
                        jsonPath(DATE_OF_CREATION).exists()
                ).andDo(result -> {
                    Article article = getArticleFromMvcResult(result);
                    assertTrue(isArticleMatchesNewArticlePayload(article, newArticlePayload));
                });
    }

    @Test
    public void createArticleWithoutAccess() throws Exception {
        NewArticlePayload newArticlePayload = newArticlePayloadTestDataGenerator.generateUnsavedData();
        var request = post(ARTICLES_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(registeredUser))
                .content(newArticlePayloadJsonTester.write(newArticlePayload).getJson());

        mockMvc.perform(request)
                .andExpect(status().isForbidden());
    }


    @Test
    public void createArticleWithInvalidData() throws Exception {
        NewArticlePayload newArticlePayload = new NewArticlePayload("", "", new ArrayList<>());
        var request = post(ARTICLES_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(registeredAdmin))
                .content(newArticlePayloadJsonTester.write(newArticlePayload).getJson());

        mockMvc.perform(request)
                .andExpectAll(
                        status().isUnprocessableEntity(),
                        jsonPath(ERROR).value(VALIDATION_FIELD),
                        jsonPath(FIELD_ERRORS).isString()
                );
    }

    @Test
    public void createArticleWithoutAuthors() throws Exception {
        Article unsavedArticle = articleTestDataGenerator.generateUnsavedData();
        NewArticlePayload newArticlePayload = new NewArticlePayload(
                unsavedArticle.getTopic(),
                unsavedArticle.getContent(),
                new ArrayList<>());
        var request = post(ARTICLES_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(registeredAdmin))
                .content(newArticlePayloadJsonTester.write(newArticlePayload).getJson());

        mockMvc.perform(request)
                .andExpectAll(
                        status().isUnprocessableEntity(),
                        jsonPath(ERROR).value(VALIDATION_FIELD),
                        jsonPath(FIELD_ERRORS).isString()
                );
    }

    @Test
    public void createArticleWithNonExistentAuthors() throws Exception {
        Article unsavedArticle = articleTestDataGenerator.generateUnsavedData();
        List<Integer> unsavedAuthorIds = userTestDataGenerator.generateUnsavedData(10).stream()
                .map(User::getId)
                .toList();
        NewArticlePayload newArticlePayload = new NewArticlePayload(
                unsavedArticle.getTopic(),
                unsavedArticle.getContent(),
                unsavedAuthorIds
        );
        var request = post(ARTICLES_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(registeredAdmin))
                .content(newArticlePayloadJsonTester.write(newArticlePayload).getJson());

        assertThrows(ServletException.class, () -> mockMvc.perform(request));
    }

    @Test
    public void updateArticleById() throws Exception {
        Article savedArticle = articleTestDataGenerator.generateSavedData();
        UpdateArticlePayload updateArticlePayload = updateArticlePayloadTestDataGenerator.generateUnsavedData();
        var request = patch(ARTICLES_ID_PATH.formatted(savedArticle.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(registeredAdmin))
                .content(updateArticlePayloadJsonTester.write(updateArticlePayload).getJson());

        mockMvc.perform(request)
                .andExpectAll(
                        status().isOk(),
                        jsonPath(ID).value(savedArticle.getId()),
                        jsonPath(DATE_OF_CREATION).exists()
                ).andDo(result -> {
                    Article article = getArticleFromMvcResult(result);
                    assertTrue(isArticleMatchesUpdateArticlePayload(article, updateArticlePayload));
                });
    }

    @Test
    public void updateArticleByIdViaTargetUser() throws Exception {
        Article savedArticle = articleTestDataGenerator.generateSavedData();
        User authorOfArticle = userService.findAuthorsByArticleId(savedArticle.getId()).getFirst();
        UserDetails userDetailsOfRegisteredUser = userToUserDetailsConverter.convert(authorOfArticle);
        UpdateArticlePayload updateArticlePayload = updateArticlePayloadTestDataGenerator.generateUnsavedData();
        var request = patch(ARTICLES_ID_PATH.formatted(savedArticle.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(Objects.requireNonNull(userDetailsOfRegisteredUser)))
                .content(updateArticlePayloadJsonTester.write(updateArticlePayload).getJson());

        mockMvc.perform(request)
                .andExpectAll(
                        status().isOk(),
                        jsonPath(ID).value(savedArticle.getId()),
                        jsonPath(DATE_OF_CREATION).exists()
                ).andDo(result -> {
                    Article article = getArticleFromMvcResult(result);
                    assertTrue(isArticleMatchesUpdateArticlePayload(article, updateArticlePayload));
                });
    }

    @Test
    public void updateArticleByIdWithoutAccess() throws Exception {
        Article savedArticle = articleTestDataGenerator.generateSavedData();
        UpdateArticlePayload updateArticlePayload = updateArticlePayloadTestDataGenerator.generateUnsavedData();
        var request = patch(ARTICLES_ID_PATH.formatted(savedArticle.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(Objects.requireNonNull(registeredUser)))
                .content(updateArticlePayloadJsonTester.write(updateArticlePayload).getJson());

        mockMvc.perform(request)
                .andExpect(status().isForbidden());
    }

    @Test
    public void updateArticleByNonExistentId() throws Exception {
        Article unsavedArticle = articleTestDataGenerator.generateUnsavedData();
        UpdateArticlePayload updateArticlePayload = updateArticlePayloadTestDataGenerator.generateUnsavedData();
        var request = patch(ARTICLES_ID_PATH.formatted(unsavedArticle.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(registeredAdmin))
                .content(updateArticlePayloadJsonTester.write(updateArticlePayload).getJson());

        assertThrows(ServletException.class, () -> mockMvc.perform(request));
    }

    @Test
    public void updateArticleByIdWithInvalidData() throws Exception {
        Article savedArticle = articleTestDataGenerator.generateSavedData();
        UpdateArticlePayload updateArticlePayload = new UpdateArticlePayload("", "");
        var request = patch(ARTICLES_ID_PATH.formatted(savedArticle.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(registeredAdmin))
                .content(updateArticlePayloadJsonTester.write(updateArticlePayload).getJson());

        mockMvc.perform(request)
                .andExpectAll(
                        status().isUnprocessableEntity(),
                        jsonPath(ERROR).value(VALIDATION_FIELD),
                        jsonPath(FIELD_ERRORS).isString()
                );
    }

    @Test
    public void deleteArticleById() throws Exception {
        Article savedArticle = articleTestDataGenerator.generateSavedData();
        var request = delete(ARTICLES_ID_PATH.formatted(savedArticle.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(registeredAdmin));

        mockMvc.perform(request)
                .andExpect(status().isNoContent());
    }

    @Test
    public void deleteArticleByIdViaTargetUser() throws Exception {
        Article savedArticle = articleTestDataGenerator.generateSavedData();
        User authorOfArticle = userService.findAuthorsByArticleId(savedArticle.getId()).getFirst();
        UserDetails userDetailsOfRegisteredUser = userToUserDetailsConverter.convert(authorOfArticle);
        var request = delete(ARTICLES_ID_PATH.formatted(savedArticle.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(Objects.requireNonNull(userDetailsOfRegisteredUser)));

        mockMvc.perform(request)
                .andExpect(status().isNoContent());
    }


    @Test
    public void deleteArticleByIdWithoutAccess() throws Exception {
        Article savedArticle = articleTestDataGenerator.generateSavedData();
        var request = delete(ARTICLES_ID_PATH.formatted(savedArticle.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(registeredUser));

        mockMvc.perform(request)
                .andExpect(status().isForbidden());
    }

    @Test
    public void deleteArticleByNonExistentId() throws Exception {
        Article savedArticle = articleTestDataGenerator.generateSavedData();
        var request = delete(ARTICLES_ID_PATH.formatted(savedArticle.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(registeredAdmin));

        mockMvc.perform(request)
                .andExpect(status().isNoContent());
    }
    @Test
    public void findArticleById() throws Exception {
        Article savedArticle = articleTestDataGenerator.generateSavedData();
        var request = get(ARTICLES_ID_PATH.formatted(savedArticle.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(registeredAdmin));

        mockMvc.perform(request)
                .andExpect(
                        status().isOk()
                ).andDo(result -> {
                    Article article = getArticleFromMvcResult(result);
                    assertEquals(savedArticle, article);
                });
    }

    @Test
    public void findArticleByNonExistentId() throws Exception {
        Article unsavedArticle = articleTestDataGenerator.generateUnsavedData();
        var request = get(ARTICLES_ID_PATH.formatted(unsavedArticle.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(registeredAdmin));

        mockMvc.perform(request)
                .andExpect(status().isNotFound());
    }


    @Test
    public void findArticlesByAuthorId() throws Exception {
        User savedAuthor = userTestDataGenerator.generateSavedData();
        List<Article> unsavedArticles = articleTestDataGenerator.generateUnsavedData(10);
        List<Article> savedArticles = unsavedArticles.stream()
                .map(x -> articleService.create(x, List.of(savedAuthor.getId())))
                .toList();
        var request = get(ARTICLES_AUTHORSHIP_ID_PATH.formatted(savedAuthor.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(registeredAdmin));

        mockMvc.perform(request)
                .andExpectAll(
                        status().isOk(),
                        jsonPath(PATH).isArray(),
                        jsonPath(LENGTH).value(savedArticles.size())
                ).andDo(result -> {
                    String content = result.getResponse().getContentAsString();
                    List<Article> articles = articleListJsonTester.parseObject(content);
                    assertTrue(savedArticles.containsAll(articles));
                    assertTrue(articles.containsAll(savedArticles));
                });
    }

    @Test
    public void findArticlesByNonExistentAuthorId() throws Exception {
        User unsavedAuthor = userTestDataGenerator.generateUnsavedData();
        var request = get(ARTICLES_AUTHORSHIP_ID_PATH.formatted(unsavedAuthor.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(registeredAdmin));

        assertThrows(ServletException.class, () -> mockMvc.perform(request));
    }

    @Test
    public void findAllArticles() throws Exception {
        List<Article> allArticles = articleTestDataGenerator.generateSavedData(10);
        var request = get(ARTICLES_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(registeredAdmin));

        mockMvc.perform(request)
                .andExpectAll(
                        status().isOk(),
                        jsonPath(PATH).isArray(),
                        jsonPath(LENGTH).value(allArticles.size())
                ).andDo(result -> {
                    String content = result.getResponse().getContentAsString();
                    List<Article> articles = articleListJsonTester.parseObject(content);
                    assertTrue(articles.containsAll(allArticles));
                    assertTrue(allArticles.containsAll(articles));
                });
    }

    private boolean isArticleMatchesNewArticlePayload(Article article, NewArticlePayload newArticlePayload) {
        return Objects.equals(article.getTopic(), newArticlePayload.topic()) &&
                Objects.equals(article.getContent(), newArticlePayload.content());
    }

    private boolean isArticleMatchesUpdateArticlePayload(Article article, UpdateArticlePayload updateArticlePayload) {
        return Objects.equals(article.getTopic(), updateArticlePayload.topic()) &&
                Objects.equals(article.getContent(), updateArticlePayload.content());
    }

    private Article getArticleFromMvcResult(MvcResult mvcResult) throws Exception {
        String content = mvcResult.getResponse().getContentAsString();
        return articleJsonTester.parseObject(content);
    }

}
