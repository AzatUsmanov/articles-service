//package pet.articles.test.controller
//
//import jakarta.servlet.ServletException
//
//import org.junit.jupiter.api.AfterEach
//import org.junit.jupiter.api.Assertions.assertTrue
//import org.junit.jupiter.api.Assertions.assertThrows
//import org.junit.jupiter.api.BeforeEach
//import org.junit.jupiter.api.Test
//
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.beans.factory.annotation.Value
//import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
//import org.springframework.boot.test.context.SpringBootTest
//import org.springframework.boot.test.json.JacksonTester
//import org.springframework.http.MediaType
//import org.springframework.security.core.userdetails.UserDetails
//import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
//import org.springframework.test.web.servlet.MockMvc
//import org.springframework.test.web.servlet.MvcResult
//import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
//import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
//import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
//import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
//import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
//import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
//import org.springframework.core.convert.converter.Converter
//import org.springframework.test.annotation.DirtiesContext
//
//import pet.articles.controller.advice.ValidationError.ErrorTypes.INVALID_FIELD
//import pet.articles.controller.advice.ValidationError.ResponseContentTypes.LIST_OF_FIELD_ERRORS
//import pet.articles.test.controller.constant.ControllerTestConstants.JsonPaths.DATE_OF_CREATION
//import pet.articles.test.controller.constant.ControllerTestConstants.JsonPaths.ERROR
//import pet.articles.test.controller.constant.ControllerTestConstants.JsonPaths.ID
//import pet.articles.test.controller.constant.ControllerTestConstants.JsonPaths.LENGTH
//import pet.articles.test.controller.constant.ControllerTestConstants.JsonPaths.PATH
//import pet.articles.model.dto.Article
//import pet.articles.model.dto.Review
//import pet.articles.model.dto.User
//import pet.articles.model.dto.payload.ReviewPayload
//import pet.articles.model.enums.UserRole
//import pet.articles.service.UserService
//import pet.articles.test.tool.db.DBCleaner
//import pet.articles.test.tool.generator.TestDataGenerator
//import pet.articles.test.tool.producer.AuthenticationDetailsProducer
//import pet.articles.test.tool.extension.extract
//import pet.articles.test.tool.extension.isMatches
//import pet.articles.test.tool.extension.toReviewPayload
//
//@SpringBootTest
//@AutoConfigureMockMvc
//@AutoConfigureJsonTesters
//@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
//class ReviewControllerTest {
//
//    companion object {
//        const val NUM_OF_TEST_REVIEWS = 100
//    }
//
//    @Value("\${api.paths.reviews}")
//    lateinit var reviewsPath: String
//
//    @Value("#{'\${api.paths.reviews}' + '/%d'}")
//    lateinit var reviewsIdPath: String
//
//    @Value("#{'\${api.paths.reviews}' + '/users/%d'}")
//    lateinit var reviewsUsersIdPath: String
//
//    @Value("#{'\${api.paths.reviews}' + '/articles/%d'}")
//    lateinit var reviewsArticlesIdPath: String
//
//    @Autowired
//    lateinit var mockMvc: MockMvc
//
//    @Autowired
//    lateinit var dbCleaner: DBCleaner
//
//    @Autowired
//    lateinit var userService: UserService
//
//    @Autowired
//    lateinit var reviewJsonTester: JacksonTester<Review>
//
//    @Autowired
//    lateinit var reviewListJsonTester: JacksonTester<List<Review>>
//
//    @Autowired
//    lateinit var reviewPayloadJsonTester: JacksonTester<ReviewPayload>
//
//    @Autowired
//    lateinit var reviewTestDataGenerator: TestDataGenerator<Review>
//
//    @Autowired
//    lateinit var reviewPayloadTestDataGenerator: TestDataGenerator<ReviewPayload>
//
//    @Autowired
//    lateinit var userTestDataGenerator: TestDataGenerator<User>
//
//    @Autowired
//    lateinit var articleTestDataGenerator: TestDataGenerator<Article>
//
//    @Autowired
//    lateinit var userToUserDetailsConverter: Converter<User, UserDetails>
//
//    @Autowired
//    lateinit var authenticationDetailsProducer: AuthenticationDetailsProducer
//
//    private lateinit var registeredUser: UserDetails
//    private lateinit var registeredAdmin: UserDetails
//
//    @BeforeEach
//    fun initAuthenticationData() {
//        registeredUser = authenticationDetailsProducer.produceUserDetailsOfRegisteredUser(UserRole.ROLE_USER)
//        registeredAdmin = authenticationDetailsProducer.produceUserDetailsOfRegisteredUser(UserRole.ROLE_ADMIN)
//    }
//
//    @AfterEach
//    fun cleanDb() {
//        dbCleaner.cleanUp()
//    }
//
//    @Test
//    fun createReview() {
//        val reviewPayload: ReviewPayload = reviewPayloadTestDataGenerator.generateUnsavedData()
//        val request: MockHttpServletRequestBuilder = post(reviewsPath)
//            .contentType(MediaType.APPLICATION_JSON)
//            .with(user(registeredAdmin))
//            .content(reviewPayloadJsonTester.write(reviewPayload).json)
//
//        mockMvc.perform(request)
//            .andExpectAll(
//                status().isCreated,
//                jsonPath(ID).isNumber,
//                jsonPath(DATE_OF_CREATION).exists()
//            ).andDo { mvcResult: MvcResult ->
//                val review: Review = mvcResult.extract(reviewJsonTester)
//                assertTrue(review.isMatches(reviewPayload))
//            }
//    }
//
//    @Test
//    fun createReviewViaTargetUser() {
//        val registeredUser: User = authenticationDetailsProducer.produceRegisteredUserWithRawPassword(UserRole.ROLE_USER)
//        val userDetailsOfRegisteredUser: UserDetails = userToUserDetailsConverter.convert(registeredUser)!!
//        val unsavedReview: Review = reviewTestDataGenerator.generateUnsavedData()
//        val reviewPayload: ReviewPayload = unsavedReview.toReviewPayload().copy(
//            authorId = registeredUser.id!!
//        )
//        val request: MockHttpServletRequestBuilder = post(reviewsPath)
//            .contentType(MediaType.APPLICATION_JSON)
//            .with(user(userDetailsOfRegisteredUser))
//            .content(reviewPayloadJsonTester.write(reviewPayload).json)
//
//        mockMvc.perform(request)
//            .andExpectAll(
//                status().isCreated,
//                jsonPath(ID).isNumber,
//                jsonPath(DATE_OF_CREATION).exists()
//            ).andDo { mvcResult: MvcResult ->
//                val review: Review = mvcResult.extract(reviewJsonTester)
//                assertTrue(review.isMatches(reviewPayload))
//            }
//    }
//
//    @Test
//    fun createReviewWithoutAccess() {
//        val reviewPayload: ReviewPayload = reviewPayloadTestDataGenerator.generateUnsavedData()
//        val request: MockHttpServletRequestBuilder = post(reviewsPath)
//            .contentType(MediaType.APPLICATION_JSON)
//            .with(user(registeredUser))
//            .content(reviewPayloadJsonTester.write(reviewPayload).json)
//
//        mockMvc.perform(request)
//            .andExpect(status().isForbidden)
//    }
//
//    @Test
//    fun createReviewWithInvalidData() {
//        val unsavedReview: Review = reviewTestDataGenerator.generateUnsavedData()
//        val invalidReviewPayload: ReviewPayload = reviewPayloadTestDataGenerator.generateInvalidData()
//        val request: MockHttpServletRequestBuilder = post(reviewsPath)
//            .contentType(MediaType.APPLICATION_JSON)
//            .with(user(registeredAdmin))
//            .content(reviewPayloadJsonTester.write(invalidReviewPayload).json)
//
//        mockMvc.perform(request)
//            .andExpectAll(
//                status().isUnprocessableEntity,
//                jsonPath(ERROR).value(INVALID_FIELD),
//                jsonPath(LIST_OF_FIELD_ERRORS).isString
//            )
//    }
//
//    @Test
//    fun createReviewWithNonExistentAuthorId() {
//        val unsavedReview: Review = reviewTestDataGenerator.generateUnsavedData()
//        val unsavedAuthor: User = userTestDataGenerator.generateUnsavedData()
//        val reviewPayload: ReviewPayload = unsavedReview.toReviewPayload().copy(
//            authorId = unsavedAuthor.id!!
//        )
//        val request: MockHttpServletRequestBuilder = post(reviewsPath)
//            .contentType(MediaType.APPLICATION_JSON)
//            .with(user(registeredAdmin))
//            .content(reviewPayloadJsonTester.write(reviewPayload).json)
//
//        assertThrows(ServletException::class.java) {
//            mockMvc.perform(request)
//        }
//    }
//
//    @Test
//    fun createReviewWithNonExistentArticleId() {
//        val unsavedReview: Review = reviewTestDataGenerator.generateUnsavedData()
//        val unsavedArticle: Article = articleTestDataGenerator.generateUnsavedData()
//        val reviewPayload: ReviewPayload = unsavedReview.toReviewPayload().copy(
//            articleId = unsavedArticle.id!!
//        )
//        val request: MockHttpServletRequestBuilder = post(reviewsPath)
//            .contentType(MediaType.APPLICATION_JSON)
//            .with(user(registeredAdmin))
//            .content(reviewPayloadJsonTester.write(reviewPayload).json)
//
//        assertThrows(ServletException::class.java) {
//            mockMvc.perform(request)
//        }
//    }
//
//    @Test
//    fun deleteReviewById() {
//        val savedReview: Review = reviewTestDataGenerator.generateSavedData()
//        val request: MockHttpServletRequestBuilder = delete(reviewsIdPath.format(savedReview.id))
//            .contentType(MediaType.APPLICATION_JSON)
//            .with(user(registeredAdmin))
//
//        mockMvc.perform(request)
//            .andExpect(status().isNoContent)
//    }
//
//    @Test
//    fun deleteReviewByIdViaTargetUser() {
//        val savedReview: Review = reviewTestDataGenerator.generateSavedData()
//        val authorOfReview: User = userService.findById(savedReview.authorId!!)!!
//        val userDetailsOfRegisteredUser: UserDetails = userToUserDetailsConverter.convert(authorOfReview)!!
//        val request: MockHttpServletRequestBuilder = delete(reviewsIdPath.format(savedReview.id))
//            .contentType(MediaType.APPLICATION_JSON)
//            .with(user(userDetailsOfRegisteredUser))
//
//        mockMvc.perform(request)
//            .andExpect(status().isNoContent)
//    }
//
//    @Test
//    fun deleteReviewByIdWithoutAccess() {
//        val savedReview: Review = reviewTestDataGenerator.generateSavedData()
//        val request: MockHttpServletRequestBuilder = delete(reviewsIdPath.format(savedReview.id))
//            .contentType(MediaType.APPLICATION_JSON)
//            .with(user(registeredUser))
//
//        mockMvc.perform(request)
//            .andExpect(status().isForbidden)
//    }
//
//    @Test
//    fun deleteReviewByNonExistentId() {
//        val unsavedReview: Review = reviewTestDataGenerator.generateUnsavedData()
//        val request: MockHttpServletRequestBuilder = delete(reviewsIdPath.format(unsavedReview.id!!))
//            .contentType(MediaType.APPLICATION_JSON)
//            .with(user(registeredUser))
//
//        mockMvc.perform(request)
//            .andExpect(status().isNoContent)
//    }
//
//    @Test
//    fun findReviewById() {
//        val savedReview: Review = reviewTestDataGenerator.generateSavedData()
//        val request: MockHttpServletRequestBuilder = get(reviewsIdPath.format(savedReview.id))
//            .contentType(MediaType.APPLICATION_JSON)
//            .with(user(registeredAdmin))
//
//        mockMvc.perform(request)
//            .andExpect(status().isOk)
//            .andDo { mvcResult: MvcResult ->
//                val review: Review = mvcResult.extract(reviewJsonTester)
//                assertTrue(savedReview == review)
//            }
//    }
//
//    @Test
//    fun findReviewByNonExistentId() {
//        val unsavedReview: Review = reviewTestDataGenerator.generateUnsavedData()
//        val request: MockHttpServletRequestBuilder = get(reviewsIdPath.format(unsavedReview.id!!))
//            .contentType(MediaType.APPLICATION_JSON)
//            .with(user(registeredAdmin))
//
//        mockMvc.perform(request)
//            .andExpect(status().isNotFound)
//    }
//
//    @Test
//    fun findReviewsByAuthorId() {
//        val savedReviews: List<Review> = reviewTestDataGenerator.generateSavedData(NUM_OF_TEST_REVIEWS)
//        val authorId: Int = savedReviews.random().authorId!!
//        val reviewsWrittenByAuthor: List<Review> = savedReviews.filter { it.authorId == authorId }
//        val request: MockHttpServletRequestBuilder = get(reviewsUsersIdPath.format(authorId))
//            .contentType(MediaType.APPLICATION_JSON)
//            .with(user(registeredAdmin))
//
//        mockMvc.perform(request)
//            .andExpectAll(
//                status().isOk,
//                jsonPath(PATH).isArray,
//                jsonPath(LENGTH).value(reviewsWrittenByAuthor.size)
//            ).andDo { mvcResult: MvcResult ->
//                val reviews: List<Review> = mvcResult.extract(reviewListJsonTester)
//                assertTrue(reviews.containsAll(reviewsWrittenByAuthor))
//                assertTrue(reviewsWrittenByAuthor.containsAll(reviews))
//            }
//    }
//
//    @Test
//    fun findReviewsByNonExistentAuthorId() {
//        val unsavedUser: User = userTestDataGenerator.generateUnsavedData()
//        val request: MockHttpServletRequestBuilder = get(reviewsUsersIdPath.format(unsavedUser.id!!))
//            .contentType(MediaType.APPLICATION_JSON)
//            .with(user(registeredAdmin))
//
//        mockMvc.perform(request)
//            .andExpect(status().isOk)
//    }
//
//    @Test
//    fun findReviewsByArticleId() {
//        val savedReviews: List<Review> = reviewTestDataGenerator.generateSavedData(NUM_OF_TEST_REVIEWS)
//        val articleId: Int = savedReviews.first().articleId
//        val reviewsWrittenForArticle: List<Review> = savedReviews.filter { it.articleId == articleId }
//        val request: MockHttpServletRequestBuilder = get(reviewsArticlesIdPath.format(articleId))
//            .contentType(MediaType.APPLICATION_JSON)
//            .with(user(registeredAdmin))
//
//        mockMvc.perform(request)
//            .andExpectAll(
//                status().isOk,
//                jsonPath(PATH).isArray,
//                jsonPath(LENGTH).value(reviewsWrittenForArticle.size)
//            ).andDo { mvcResult: MvcResult ->
//                val reviews: List<Review> = mvcResult.extract(reviewListJsonTester)
//                assertTrue(reviews.containsAll(reviewsWrittenForArticle))
//                assertTrue(reviewsWrittenForArticle.containsAll(reviews))
//            }
//    }
//
//    @Test
//    fun findReviewsByNonExistentArticleId() {
//        val unsavedArticle: Article = articleTestDataGenerator.generateUnsavedData()
//        val request: MockHttpServletRequestBuilder = get(reviewsArticlesIdPath.format(unsavedArticle.id!!))
//            .contentType(MediaType.APPLICATION_JSON)
//            .with(user(registeredAdmin))
//
//        mockMvc.perform(request)
//            .andExpect(status().isOk)
//    }
//}