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
//import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
//import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
//import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
//import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
//import org.springframework.core.convert.converter.Converter
//
//import pet.articles.model.dto.Article
//import pet.articles.model.dto.User
//import pet.articles.model.dto.payload.NewArticlePayload
//import pet.articles.model.dto.payload.UpdateArticlePayload
//import pet.articles.model.enums.UserRole
//import pet.articles.service.ArticleService
//import pet.articles.service.UserService
//import pet.articles.test.tool.db.DBCleaner
//import pet.articles.test.tool.generator.TestDataGenerator
//import pet.articles.test.tool.producer.AuthenticationDetailsProducer
//import pet.articles.controller.advice.ValidationError.ErrorTypes.INVALID_FIELD
//import pet.articles.controller.advice.ValidationError.ResponseContentTypes.LIST_OF_FIELD_ERRORS
//import pet.articles.test.controller.constant.ControllerTestConstants.JsonPaths.DATE_OF_CREATION
//import pet.articles.test.controller.constant.ControllerTestConstants.JsonPaths.ERROR
//import pet.articles.test.controller.constant.ControllerTestConstants.JsonPaths.ID
//import pet.articles.test.controller.constant.ControllerTestConstants.JsonPaths.LENGTH
//import pet.articles.test.controller.constant.ControllerTestConstants.JsonPaths.PATH
//import pet.articles.test.tool.extension.extract
//import pet.articles.test.tool.extension.isMatches
//import pet.articles.test.tool.extension.toNewArticlePayload
//import pet.articles.test.tool.extension.toUpdateArticlePayload
//
//@SpringBootTest
//@AutoConfigureMockMvc
//@AutoConfigureJsonTesters
//class ArticleControllerTest {
//
//    companion object {
//        const val NUM_OF_TEST_ARTICLES = 10
//    }
//
//    @Value("\${api.paths.articles}")
//    lateinit var articlesPath: String
//
//    @Value("#{'\${api.paths.articles}' + '/%d'}")
//    lateinit var articlesIdPath: String
//
//    @Value("#{'\${api.paths.articles}' + '/authorship/%d'}")
//    lateinit var articlesAuthorshipIdPath: String
//
//    @Autowired
//    lateinit var mockMvc: MockMvc
//
//    @Autowired
//    lateinit var dbCleaner: DBCleaner
//
//    @Autowired
//    lateinit var articleService: ArticleService
//
//    @Autowired
//    lateinit var userService: UserService
//
//    @Autowired
//    lateinit var articleJsonTester: JacksonTester<Article>
//
//    @Autowired
//    lateinit var articleListJsonTester: JacksonTester<List<Article>>
//
//    @Autowired
//    lateinit var newArticlePayloadJsonTester: JacksonTester<NewArticlePayload>
//
//    @Autowired
//    lateinit var updateArticlePayloadJsonTester: JacksonTester<UpdateArticlePayload>
//
//    @Autowired
//    lateinit var articleTestDataGenerator: TestDataGenerator<Article>
//
//    @Autowired
//    lateinit var newArticlePayloadTestDataGenerator: TestDataGenerator<NewArticlePayload>
//
//    @Autowired
//    lateinit var updateArticlePayloadTestDataGenerator: TestDataGenerator<UpdateArticlePayload>
//
//    @Autowired
//    lateinit var userTestDataGenerator: TestDataGenerator<User>
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
//    fun createArticle() {
//        val newArticlePayload: NewArticlePayload = newArticlePayloadTestDataGenerator.generateUnsavedData()
//        val request: MockHttpServletRequestBuilder = post(articlesPath)
//            .contentType(MediaType.APPLICATION_JSON)
//            .with(user(registeredAdmin))
//            .content(newArticlePayloadJsonTester.write(newArticlePayload).json)
//
//        mockMvc.perform(request)
//            .andExpectAll(
//                status().isCreated,
//                jsonPath(ID).isNumber,
//                jsonPath(DATE_OF_CREATION).exists()
//            ).andDo { mvcResult: MvcResult ->
//                val article: Article = mvcResult.extract(articleJsonTester)
//                assertTrue(article.isMatches(newArticlePayload))
//            }
//    }
//
//    @Test
//    fun createArticleViaTargetUser() {
//        val newArticlePayload: NewArticlePayload = newArticlePayloadTestDataGenerator.generateUnsavedData()
//        val registeredUser: User = authenticationDetailsProducer.produceRegisteredUserWithRawPassword(UserRole.ROLE_USER)
//        val userDetailsOfRegisteredUser: UserDetails = userToUserDetailsConverter.convert(registeredUser)!!
//        val updatedPayload: NewArticlePayload = newArticlePayload.copy(
//            authorIds = newArticlePayload.authorIds + registeredUser.id!!
//        )
//        val request: MockHttpServletRequestBuilder = post(articlesPath)
//            .contentType(MediaType.APPLICATION_JSON)
//            .with(user(userDetailsOfRegisteredUser))
//            .content(newArticlePayloadJsonTester.write(updatedPayload).json)
//
//        mockMvc.perform(request)
//            .andExpectAll(
//                status().isCreated,
//                jsonPath(ID).isNumber,
//                jsonPath(DATE_OF_CREATION).exists()
//            ).andDo { mvcResult: MvcResult ->
//                val article: Article = mvcResult.extract(articleJsonTester)
//                assertTrue(article.isMatches(updatedPayload))
//            }
//    }
//
//    @Test
//    fun createArticleWithoutAccess() {
//        val newArticlePayload: NewArticlePayload = newArticlePayloadTestDataGenerator.generateUnsavedData()
//        val request: MockHttpServletRequestBuilder = post(articlesPath)
//            .contentType(MediaType.APPLICATION_JSON)
//            .with(user(registeredUser))
//            .content(newArticlePayloadJsonTester.write(newArticlePayload).json)
//
//        mockMvc.perform(request)
//            .andExpect(status().isForbidden)
//    }
//
//    @Test
//    fun createArticleWithInvalidData() {
//        val invalidNewArticlePayload: NewArticlePayload = newArticlePayloadTestDataGenerator.generateInvalidData()
//        val request: MockHttpServletRequestBuilder = post(articlesPath)
//            .contentType(MediaType.APPLICATION_JSON)
//            .with(user(registeredAdmin))
//            .content(newArticlePayloadJsonTester.write(invalidNewArticlePayload).json)
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
//    fun createArticleWithoutAuthors() {
//        val unsavedArticle: Article = articleTestDataGenerator.generateUnsavedData()
//        val newArticlePayload: NewArticlePayload = unsavedArticle.toNewArticlePayload(emptyList())
//        val request: MockHttpServletRequestBuilder = post(articlesPath)
//            .contentType(MediaType.APPLICATION_JSON)
//            .with(user(registeredAdmin))
//            .content(newArticlePayloadJsonTester.write(newArticlePayload).json)
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
//    fun createArticleWithNonExistentAuthors() {
//        val unsavedArticle: Article = articleTestDataGenerator.generateUnsavedData()
//        val unsavedAuthorIds: List<Int> = userTestDataGenerator.generateUnsavedData(NUM_OF_TEST_ARTICLES)
//            .map { user -> user.id!! }
//        val newArticlePayload: NewArticlePayload = unsavedArticle.toNewArticlePayload(unsavedAuthorIds)
//        val request: MockHttpServletRequestBuilder = post(articlesPath)
//            .contentType(MediaType.APPLICATION_JSON)
//            .with(user(registeredAdmin))
//            .content(newArticlePayloadJsonTester.write(newArticlePayload).json)
//
//        assertThrows(ServletException::class.java) {
//            mockMvc.perform(request)
//        }
//    }
//
//    @Test
//    fun updateArticleById() {
//        val savedArticle: Article = articleTestDataGenerator.generateSavedData()
//        val updateArticlePayload: UpdateArticlePayload = savedArticle.toUpdateArticlePayload()
//        val request: MockHttpServletRequestBuilder = patch(articlesIdPath.format(savedArticle.id))
//            .contentType(MediaType.APPLICATION_JSON)
//            .with(user(registeredAdmin))
//            .content(updateArticlePayloadJsonTester.write(updateArticlePayload).json)
//
//        mockMvc.perform(request)
//            .andExpectAll(
//                status().isOk,
//                jsonPath(ID).value(savedArticle.id),
//                jsonPath(DATE_OF_CREATION).exists()
//            ).andDo { mvcResult: MvcResult ->
//                val article: Article = mvcResult.extract(articleJsonTester)
//                assertTrue(article.isMatches(updateArticlePayload))
//            }
//    }
//
//    @Test
//    fun updateArticleByIdViaTargetUser() {
//        val savedArticle: Article = articleTestDataGenerator.generateSavedData()
//        val authorOfArticle: User = userService.findAuthorsByArticleId(savedArticle.id!!).first()
//        val userDetailsOfRegisteredUser: UserDetails = userToUserDetailsConverter.convert(authorOfArticle)!!
//        val updateArticlePayload: UpdateArticlePayload = savedArticle.toUpdateArticlePayload()
//        val request: MockHttpServletRequestBuilder = patch(articlesIdPath.format(savedArticle.id))
//            .contentType(MediaType.APPLICATION_JSON)
//            .with(user(userDetailsOfRegisteredUser))
//            .content(updateArticlePayloadJsonTester.write(updateArticlePayload).json)
//
//        mockMvc.perform(request)
//            .andExpectAll(
//                status().isOk,
//                jsonPath(ID).value(savedArticle.id),
//                jsonPath(DATE_OF_CREATION).exists()
//            ).andDo { mvcResult: MvcResult ->
//                val article: Article = mvcResult.extract(articleJsonTester)
//                assertTrue(article.isMatches(updateArticlePayload))
//            }
//    }
//
//    @Test
//    fun updateArticleByIdWithoutAccess() {
//        val savedArticle: Article = articleTestDataGenerator.generateSavedData()
//        val updateArticlePayload: UpdateArticlePayload = updateArticlePayloadTestDataGenerator.generateUnsavedData()
//        val request: MockHttpServletRequestBuilder = patch(articlesIdPath.format(savedArticle.id))
//            .contentType(MediaType.APPLICATION_JSON)
//            .with(user(registeredUser))
//            .content(updateArticlePayloadJsonTester.write(updateArticlePayload).json)
//
//        mockMvc.perform(request)
//            .andExpect(status().isForbidden)
//    }
//
//    @Test
//    fun updateArticleByNonExistentId() {
//        val unsavedArticle: Article = articleTestDataGenerator.generateUnsavedData()
//        val updateArticlePayload: UpdateArticlePayload = updateArticlePayloadTestDataGenerator.generateUnsavedData()
//        val request: MockHttpServletRequestBuilder = patch(articlesIdPath.format(unsavedArticle.id!!))
//            .contentType(MediaType.APPLICATION_JSON)
//            .with(user(registeredAdmin))
//            .content(updateArticlePayloadJsonTester.write(updateArticlePayload).json)
//
//        assertThrows(ServletException::class.java) {
//            mockMvc.perform(request)
//        }
//    }
//
//    @Test
//    fun updateArticleByIdWithInvalidData() {
//        val savedArticle: Article = articleTestDataGenerator.generateSavedData()
//        val invalidUpdateArticlePayload: UpdateArticlePayload = updateArticlePayloadTestDataGenerator.generateInvalidData()
//        val request: MockHttpServletRequestBuilder = patch(articlesIdPath.format(savedArticle.id))
//            .contentType(MediaType.APPLICATION_JSON)
//            .with(user(registeredAdmin))
//            .content(updateArticlePayloadJsonTester.write(invalidUpdateArticlePayload).json)
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
//    fun deleteArticleById() {
//        val savedArticle: Article = articleTestDataGenerator.generateSavedData()
//        val request: MockHttpServletRequestBuilder = delete(articlesIdPath.format(savedArticle.id))
//            .contentType(MediaType.APPLICATION_JSON)
//            .with(user(registeredAdmin))
//
//        mockMvc.perform(request)
//            .andExpect(status().isNoContent)
//    }
//
//    @Test
//    fun deleteArticleByIdViaTargetUser() {
//        val savedArticle: Article = articleTestDataGenerator.generateSavedData()
//        val authorOfArticle: User = userService.findAuthorsByArticleId(savedArticle.id!!).first()
//        val userDetailsOfRegisteredUser: UserDetails = userToUserDetailsConverter.convert(authorOfArticle)!!
//        val request: MockHttpServletRequestBuilder = delete(articlesIdPath.format(savedArticle.id))
//            .contentType(MediaType.APPLICATION_JSON)
//            .with(user(userDetailsOfRegisteredUser))
//
//        mockMvc.perform(request)
//            .andExpect(status().isNoContent)
//    }
//
//    @Test
//    fun deleteArticleByIdWithoutAccess() {
//        val savedArticle: Article = articleTestDataGenerator.generateSavedData()
//        val request: MockHttpServletRequestBuilder = delete(articlesIdPath.format(savedArticle.id))
//            .contentType(MediaType.APPLICATION_JSON)
//            .with(user(registeredUser))
//
//        mockMvc.perform(request)
//            .andExpect(status().isForbidden)
//    }
//
//    @Test
//    fun deleteArticleByNonExistentId() {
//        val savedArticle: Article = articleTestDataGenerator.generateSavedData()
//        val request: MockHttpServletRequestBuilder = delete(articlesIdPath.format(savedArticle.id))
//            .contentType(MediaType.APPLICATION_JSON)
//            .with(user(registeredAdmin))
//
//        mockMvc.perform(request)
//            .andExpect(status().isNoContent)
//    }
//
//    @Test
//    fun findArticleById() {
//        val savedArticle: Article = articleTestDataGenerator.generateSavedData()
//        val request: MockHttpServletRequestBuilder = get(articlesIdPath.format(savedArticle.id))
//            .contentType(MediaType.APPLICATION_JSON)
//            .with(user(registeredAdmin))
//
//        mockMvc.perform(request)
//            .andExpect(status().isOk)
//            .andDo { mvcResult: MvcResult ->
//                val article: Article = mvcResult.extract(articleJsonTester)
//                assertTrue(savedArticle == article)
//            }
//    }
//
//    @Test
//    fun findArticleByNonExistentId() {
//        val unsavedArticle: Article = articleTestDataGenerator.generateUnsavedData()
//        val request: MockHttpServletRequestBuilder = get(articlesIdPath.format(unsavedArticle.id!!))
//            .contentType(MediaType.APPLICATION_JSON)
//            .with(user(registeredAdmin))
//
//        mockMvc.perform(request)
//            .andExpect(status().isNotFound)
//    }
//
//    @Test
//    fun findArticlesByAuthorId() {
//        val savedAuthor: User = userTestDataGenerator.generateSavedData()
//        val unsavedArticles: List<Article> = articleTestDataGenerator.generateUnsavedData(NUM_OF_TEST_ARTICLES)
//        val savedArticles: List<Article> = unsavedArticles.map {
//            articleService.create(it, listOf(savedAuthor.id!!))
//        }
//        val request: MockHttpServletRequestBuilder = get(articlesAuthorshipIdPath.format(savedAuthor.id))
//            .contentType(MediaType.APPLICATION_JSON)
//            .with(user(registeredAdmin))
//
//        mockMvc.perform(request)
//            .andExpectAll(
//                status().isOk,
//                jsonPath(PATH).isArray,
//                jsonPath(LENGTH).value(savedArticles.size)
//            ).andDo { mvcResult: MvcResult ->
//                val articles: List<Article> = mvcResult.extract(articleListJsonTester)
//                assertTrue(savedArticles.containsAll(articles))
//                assertTrue(articles.containsAll(savedArticles))
//            }
//    }
//
//    @Test
//    fun findArticlesByNonExistentAuthorId() {
//        val unsavedAuthor: User = userTestDataGenerator.generateUnsavedData()
//        val request: MockHttpServletRequestBuilder = get(articlesAuthorshipIdPath.format(unsavedAuthor.id!!))
//            .contentType(MediaType.APPLICATION_JSON)
//            .with(user(registeredAdmin))
//
//        mockMvc.perform(request)
//            .andExpect(status().isOk)
//    }
//
//    @Test
//    fun findAllArticles() {
//        val allArticles: List<Article> = articleTestDataGenerator.generateSavedData(NUM_OF_TEST_ARTICLES)
//        val request: MockHttpServletRequestBuilder = get(articlesPath)
//            .contentType(MediaType.APPLICATION_JSON)
//            .with(user(registeredAdmin))
//
//        mockMvc.perform(request)
//            .andExpectAll(
//                status().isOk,
//                jsonPath(PATH).isArray,
//                jsonPath(LENGTH).value(allArticles.size)
//            ).andDo { mvcResult: MvcResult ->
//                val articles: List<Article> = mvcResult.extract(articleListJsonTester)
//                assertTrue(articles.containsAll(allArticles))
//                assertTrue(allArticles.containsAll(articles))
//            }
//    }
//}