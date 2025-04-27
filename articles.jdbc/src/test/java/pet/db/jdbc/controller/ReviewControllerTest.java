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

import pet.db.jdbc.model.dto.payload.ReviewPayload;
import pet.db.jdbc.model.dto.Article;
import pet.db.jdbc.model.dto.Review;
import pet.db.jdbc.model.dto.User;
import pet.db.jdbc.model.enums.UserRole;
import pet.db.jdbc.service.UserService;
import pet.db.jdbc.tool.producer.AuthenticationDetailsProducer;
import pet.db.jdbc.tool.db.DbCleaner;
import pet.db.jdbc.tool.generator.TestDataGenerator;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
public class ReviewControllerTest {

    @Value("${api.paths.reviews}")
    public String REVIEWS_PATH;

    @Value("#{'${api.paths.reviews}' + '/%d'}")
    public String REVIEWS_ID_PATH;

    @Value("#{'${api.paths.reviews}' + '/users/%d'}")
    public String REVIEWS_USERS_ID_PATH;

    @Value("#{'${api.paths.reviews}' + '/articles/%d'}")
    public String REVIEWS_ARTICLES_ID_PATH;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DbCleaner dbCleaner;

    @Autowired
    private UserService userService;

    @Autowired
    private JacksonTester<Review> reviewJsonTester;

    @Autowired
    private JacksonTester<List<Review>> reviewListJsonTester;

    @Autowired
    private JacksonTester<ReviewPayload> reviewPayloadJsonTester;

    @Autowired
    private TestDataGenerator<Review> reviewTestDataGenerator;

    @Autowired
    private TestDataGenerator<ReviewPayload> reviewPayloadTestDataGenerator;

    @Autowired
    private TestDataGenerator<User> userTestDataGenerator;

    @Autowired
    private TestDataGenerator<Article> articleTestDataGenerator;

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
    void cleanDb() {
        dbCleaner.cleanAll();
    }

    @Test
    public void createReview() throws Exception {
        ReviewPayload reviewPayload = reviewPayloadTestDataGenerator.generateUnsavedData();
        var request = post(REVIEWS_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(registeredAdmin))
                .content(reviewPayloadJsonTester.write(reviewPayload).getJson());

        mockMvc.perform(request)
                .andExpectAll(
                        status().isCreated(),
                        jsonPath(ID).isNumber(),
                        jsonPath(DATE_OF_CREATION).exists()
                ).andDo(result -> {
                    Review review = getReviewFromMvcResult(result);
                    assertTrue(isReviewMatchesReviewPayload(review, reviewPayload));
                });
    }

    @Test
    public void createReviewViaTargetUser() throws Exception {
        User registeredUser = authenticationDataGenerator.produceRegisteredUserWithRawPassword(UserRole.ROLE_USER);
        UserDetails userDetailsOfRegisteredUser = userToUserDetailsConverter.convert(registeredUser);
        Review unsavedReview = reviewTestDataGenerator.generateUnsavedData();
        ReviewPayload reviewPayload = new ReviewPayload(
                unsavedReview.getType(),
                unsavedReview.getContent(),
                registeredUser.getId(),
                unsavedReview.getArticleId()
        );
        var request = post(REVIEWS_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(Objects.requireNonNull(userDetailsOfRegisteredUser)))
                .content(reviewPayloadJsonTester.write(reviewPayload).getJson());

        mockMvc.perform(request)
                .andExpectAll(
                        status().isCreated(),
                        jsonPath(ID).isNumber(),
                        jsonPath(DATE_OF_CREATION).exists()
                ).andDo(result -> {
                    Review review = getReviewFromMvcResult(result);
                    assertTrue(isReviewMatchesReviewPayload(review, reviewPayload));
                });
    }

    @Test
    public void createReviewWithoutAccess() throws Exception {
        ReviewPayload reviewPayload = reviewPayloadTestDataGenerator.generateUnsavedData();
        var request = post(REVIEWS_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(registeredUser))
                .content(reviewPayloadJsonTester.write(reviewPayload).getJson());

        mockMvc.perform(request)
                .andExpect(status().isForbidden());
    }

    @Test
    public void createReviewWithInvalidData() throws Exception {
        Review unsavedReview = reviewTestDataGenerator.generateUnsavedData();
        ReviewPayload reviewPayload = new ReviewPayload(
                unsavedReview.getType(),
                "",
                unsavedReview.getAuthorId(),
                unsavedReview.getArticleId()
        );
        var request = post(REVIEWS_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(registeredAdmin))
                .content(reviewPayloadJsonTester.write(reviewPayload).getJson());

        mockMvc.perform(request)
                .andExpectAll(
                        status().isUnprocessableEntity(),
                        jsonPath(ERROR).value(VALIDATION_FIELD),
                        jsonPath(FIELD_ERRORS).isString()
                );
    }

    @Test
    public void createReviewWithNonExistentAuthorId() throws Exception {
        Review unsavedReview = reviewTestDataGenerator.generateUnsavedData();
        User unsavedAuthor = userTestDataGenerator.generateUnsavedData();
        ReviewPayload reviewPayload = new ReviewPayload(
                unsavedReview.getType(),
                unsavedReview.getContent(),
                unsavedAuthor.getId(),
                unsavedReview.getArticleId()
        );
        var request = post(REVIEWS_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(registeredAdmin))
                .content(reviewPayloadJsonTester.write(reviewPayload).getJson());

        assertThrows(ServletException.class, () -> mockMvc.perform(request));
    }

    @Test
    public void createReviewWithNonExistentArticleId() throws Exception {
        Review unsavedReview = reviewTestDataGenerator.generateUnsavedData();
        Article unsavedArticle = articleTestDataGenerator.generateUnsavedData();
        ReviewPayload reviewPayload = new ReviewPayload(
                unsavedReview.getType(),
                unsavedReview.getContent(),
                unsavedReview.getAuthorId(),
                unsavedArticle.getId()
        );
        var request = post(REVIEWS_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(registeredAdmin))
                .content(reviewPayloadJsonTester.write(reviewPayload).getJson());

        assertThrows(ServletException.class, () -> mockMvc.perform(request));
    }


    @Test
    public void deleteReviewById() throws Exception {
        Review savedReview = reviewTestDataGenerator.generateSavedData();
        var request = delete(REVIEWS_ID_PATH.formatted(savedReview.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(registeredAdmin));

        mockMvc.perform(request)
                .andExpect(status().isNoContent());
    }

    @Test
    public void deleteReviewByIdViaTargetUser() throws Exception {
        Review savedReview = reviewTestDataGenerator.generateSavedData();
        User authorOfReview = userService.findById(savedReview.getAuthorId()).orElseThrow(NoSuchElementException::new);
        UserDetails userDetailsOfRegisteredUser = userToUserDetailsConverter.convert(authorOfReview);
        var request = delete(REVIEWS_ID_PATH.formatted(savedReview.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(Objects.requireNonNull(userDetailsOfRegisteredUser)));

        mockMvc.perform(request)
                .andExpect(status().isNoContent());
    }

    @Test
    public void deleteReviewByIdWithoutAccess() throws Exception {
        Review savedReview = reviewTestDataGenerator.generateSavedData();
        var request = delete(REVIEWS_ID_PATH.formatted(savedReview.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(registeredUser));

        mockMvc.perform(request)
                .andExpect(status().isForbidden());
    }

    @Test
    public void deleteReviewByNonExistentId() throws Exception {
        Review unsavedReview = reviewTestDataGenerator.generateUnsavedData();
        var request = delete(REVIEWS_ID_PATH.formatted(unsavedReview.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(registeredUser));

        mockMvc.perform(request)
                .andExpect(status().isNoContent());
    }

    @Test
    public void findReviewById() throws Exception {
        Review savedReview = reviewTestDataGenerator.generateSavedData();
        var request = get(REVIEWS_ID_PATH.formatted(savedReview.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(registeredAdmin));

        mockMvc.perform(request)
                .andExpect(
                        status().isOk()
                ).andDo(result -> {
                    Review review = getReviewFromMvcResult(result);
                    assertEquals(savedReview, review);
                });
    }

    @Test
    public void findReviewByNonExistentId() throws Exception {
        Review unsavedReview = reviewTestDataGenerator.generateUnsavedData();
        var request = get(REVIEWS_ID_PATH.formatted(unsavedReview.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(registeredAdmin));

        mockMvc.perform(request)
                .andExpect(status().isNotFound());
    }

    @Test
    public void findReviewsByAuthorId() throws Exception {
        List<Review> savedReviews = reviewTestDataGenerator.generateSavedData(100);
        Integer authorId = savedReviews.getFirst().getAuthorId();
        List<Review> reviewsWrittenByAuthor = savedReviews.stream()
                .filter(x -> Objects.equals(x.getAuthorId(), authorId))
                .toList();
        var request = get(REVIEWS_USERS_ID_PATH.formatted(authorId))
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(registeredAdmin));

        mockMvc.perform(request)
                .andExpectAll(
                        status().isOk(),
                        jsonPath(PATH).isArray(),
                        jsonPath(LENGTH).value(reviewsWrittenByAuthor.size())
                ).andDo(result -> {
                    List<Review> reviews = getReviewListFromMvcResult(result);
                    assertTrue(reviews.containsAll(reviewsWrittenByAuthor));
                    assertTrue(reviewsWrittenByAuthor.containsAll(reviews));
                });
    }

    @Test
    public void findReviewsByNonExistentAuthorId() {
        User unsavedUser = userTestDataGenerator.generateUnsavedData();
        var request = get(REVIEWS_USERS_ID_PATH.formatted(unsavedUser.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(registeredAdmin));

        assertThrows(ServletException.class, () -> mockMvc.perform(request));
    }


    @Test
    public void findReviewsByArticleId() throws Exception {
        List<Review> savedReviews = reviewTestDataGenerator.generateSavedData(100);
        Integer articleId = savedReviews.getFirst().getArticleId();
        List<Review> reviewsWrittenForArticle = savedReviews.stream()
                .filter(x -> Objects.equals(x.getArticleId(), articleId))
                .toList();
        var request = get(REVIEWS_ARTICLES_ID_PATH.formatted(articleId))
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(registeredAdmin));

        mockMvc.perform(request)
                .andExpectAll(
                        status().isOk(),
                        jsonPath(PATH).isArray(),
                        jsonPath(LENGTH).value(reviewsWrittenForArticle.size())
                ).andDo(result -> {
                    List<Review> reviews = getReviewListFromMvcResult(result);
                    assertTrue(reviews.containsAll(reviewsWrittenForArticle));
                    assertTrue(reviewsWrittenForArticle.containsAll(reviews));
                });
    }

    @Test
    public void findReviewsByNonExistentArticleId() {
        Article unsavedArticle = articleTestDataGenerator.generateUnsavedData();
        var request = get(REVIEWS_ARTICLES_ID_PATH.formatted(unsavedArticle.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(registeredAdmin));

        assertThrows(ServletException.class, () -> mockMvc.perform(request));
    }


    private boolean isReviewMatchesReviewPayload(Review review, ReviewPayload reviewPayload) {
        return Objects.equals(review.getType(), reviewPayload.type()) &&
                Objects.equals(review.getContent(), reviewPayload.content()) &&
                 Objects.equals(review.getAuthorId(), reviewPayload.authorId()) &&
                  Objects.equals(review.getArticleId(), reviewPayload.articleId());
    }

    private Review getReviewFromMvcResult(MvcResult mvcResult) throws Exception {
        String content = mvcResult.getResponse().getContentAsString();
        return reviewJsonTester.parseObject(content);
    }

    private List<Review> getReviewListFromMvcResult(MvcResult mvcResult) throws Exception {
        String content = mvcResult.getResponse().getContentAsString();
        return reviewListJsonTester.parseObject(content);
    }

}
