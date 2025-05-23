//package pet.articles.test.controller
//
//import jakarta.servlet.ServletException
//
//import org.junit.jupiter.api.AfterEach
//import org.junit.jupiter.api.Assertions.assertEquals
//import org.junit.jupiter.api.Assertions.assertThrows
//import org.junit.jupiter.api.Assertions.assertTrue
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
//import org.springframework.core.convert.converter.Converter
//import org.springframework.test.annotation.DirtiesContext
//import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
//import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
//import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
//import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
//import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
//import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
//
//import pet.articles.model.dto.Article
//import pet.articles.model.dto.User
//import pet.articles.model.dto.payload.UserPayload
//import pet.articles.model.enums.UserRole
//import pet.articles.service.ArticleService
//import pet.articles.test.tool.db.DBCleaner
//import pet.articles.test.tool.generator.TestDataGenerator
//import pet.articles.controller.advice.ValidationError.ErrorTypes.DUPLICATE_FIELD
//import pet.articles.controller.advice.ValidationError.ErrorTypes.INVALID_FIELD
//import pet.articles.controller.advice.ValidationError.ResponseContentTypes.LIST_OF_FIELD_ERRORS
//import pet.articles.test.controller.constant.ControllerTestConstants.Fields.EMAIL
//import pet.articles.test.controller.constant.ControllerTestConstants.Fields.USERNAME
//import pet.articles.test.controller.constant.ControllerTestConstants.JsonPaths.ERROR
//import pet.articles.test.controller.constant.ControllerTestConstants.JsonPaths.FIELD
//import pet.articles.test.controller.constant.ControllerTestConstants.JsonPaths.ID
//import pet.articles.test.controller.constant.ControllerTestConstants.JsonPaths.LENGTH
//import pet.articles.test.controller.constant.ControllerTestConstants.JsonPaths.PATH
//import pet.articles.test.tool.extension.extract
//import pet.articles.test.tool.extension.isMatches
//import pet.articles.test.tool.producer.AuthenticationDetailsProducer
//import pet.articles.test.tool.extension.toUserPayload
//
//@SpringBootTest
//@AutoConfigureMockMvc
//@AutoConfigureJsonTesters
//@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
//class UserControllerTest {
//
//    companion object {
//        const val NUM_OF_TEST_REVIEWS = 10
//    }
//
//    @Value("\${api.paths.users}")
//    lateinit var usersPath: String
//
//    @Value("#{'\${api.paths.users}' + '/%d'}")
//    lateinit var usersIdPath: String
//
//    @Value("#{'\${api.paths.users}' + '/authorship/%d'}")
//    lateinit var usersAuthorshipIdPath: String
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
//    lateinit var userJsonTester: JacksonTester<User>
//
//    @Autowired
//    lateinit var userListJsonTester: JacksonTester<List<User>>
//
//    @Autowired
//    lateinit var userPayloadJsonTester: JacksonTester<UserPayload>
//
//    @Autowired
//    lateinit var userPayloadTestDataGenerator: TestDataGenerator<UserPayload>
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
//    fun initAuthenticationDetails() {
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
//    fun createUser() {
//        val userPayload: UserPayload = userPayloadTestDataGenerator.generateUnsavedData()
//        val request: MockHttpServletRequestBuilder = post(usersPath)
//            .contentType(MediaType.APPLICATION_JSON)
//            .with(user(registeredAdmin))
//            .content(userPayloadJsonTester.write(userPayload).json)
//
//        mockMvc.perform(request)
//            .andExpectAll(
//                status().isCreated,
//                jsonPath(ID).isNumber
//            )
//            .andDo { result: MvcResult ->
//                val user: User = result.extract(userJsonTester)
//                assertTrue(user.isMatches(userPayload))
//            }
//    }
//
//    @Test
//    fun createUserWithoutAccess() {
//        val userPayload: UserPayload = userPayloadTestDataGenerator.generateUnsavedData()
//        val request: MockHttpServletRequestBuilder = post(usersPath)
//            .contentType(MediaType.APPLICATION_JSON)
//            .with(user(registeredUser))
//            .content(userPayloadJsonTester.write(userPayload).json)
//
//        mockMvc.perform(request)
//            .andExpect(status().isForbidden)
//    }
//
//    @Test
//    fun createUserWithInvalidData() {
//        val invalidUserPayload: UserPayload = userPayloadTestDataGenerator.generateInvalidData()
//        val request: MockHttpServletRequestBuilder = post(usersPath)
//            .contentType(MediaType.APPLICATION_JSON)
//            .with(user(registeredAdmin))
//            .content(userPayloadJsonTester.write(invalidUserPayload).json)
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
//    fun createUserWithNotUniqueUsername() {
//        val savedUser: User = userTestDataGenerator.generateSavedData()
//        val unsavedUser: User = userTestDataGenerator.generateUnsavedData()
//        val userPayload: UserPayload = unsavedUser.toUserPayload().copy(
//            username = savedUser.username
//        )
//        val request: MockHttpServletRequestBuilder = post(usersPath)
//            .contentType(MediaType.APPLICATION_JSON)
//            .with(user(registeredAdmin))
//            .content(userPayloadJsonTester.write(userPayload).json)
//
//        mockMvc.perform(request)
//            .andExpectAll(
//                status().isConflict,
//                jsonPath(ERROR).value(DUPLICATE_FIELD),
//                jsonPath(FIELD).value(USERNAME)
//            )
//    }
//
//    @Test
//    fun createUserWithNotUniqueEmail() {
//        val savedUser: User = userTestDataGenerator.generateSavedData()
//        val unsavedUser: User = userTestDataGenerator.generateUnsavedData()
//        val userPayload: UserPayload = unsavedUser.toUserPayload().copy(
//            email = savedUser.email
//        )
//        val request: MockHttpServletRequestBuilder = post(usersPath)
//            .contentType(MediaType.APPLICATION_JSON)
//            .with(user(registeredAdmin))
//            .content(userPayloadJsonTester.write(userPayload).json)
//
//        mockMvc.perform(request)
//            .andExpectAll(
//                status().isConflict,
//                jsonPath(ERROR).value(DUPLICATE_FIELD),
//                jsonPath(FIELD).value(EMAIL)
//            )
//    }
//
//    @Test
//    fun updateUserById() {
//        val savedUser: User = userTestDataGenerator.generateSavedData()
//        val userPayload: UserPayload = userPayloadTestDataGenerator.generateUnsavedData()
//        val request: MockHttpServletRequestBuilder = patch(usersIdPath.format(savedUser.id))
//            .contentType(MediaType.APPLICATION_JSON)
//            .with(user(registeredAdmin))
//            .content(userPayloadJsonTester.write(userPayload).json)
//
//        mockMvc.perform(request)
//            .andExpectAll(
//                status().isOk,
//                jsonPath(ID).value(savedUser.id)
//            )
//            .andDo { result: MvcResult ->
//                val user: User = result.extract(userJsonTester)
//                assertTrue(user.isMatches(userPayload))
//            }
//    }
//
//    @Test
//    fun updateUserByIdWithoutAccess() {
//        val savedUser: User = userTestDataGenerator.generateSavedData()
//        val userPayload: UserPayload = userPayloadTestDataGenerator.generateUnsavedData()
//        val request: MockHttpServletRequestBuilder = patch(usersIdPath.format(savedUser.id))
//            .contentType(MediaType.APPLICATION_JSON)
//            .with(user(registeredUser))
//            .content(userPayloadJsonTester.write(userPayload).json)
//
//        mockMvc.perform(request)
//            .andExpect(status().isForbidden)
//    }
//
//    @Test
//    fun updateUserByIdWithInvalidData() {
//        val savedUser: User = userTestDataGenerator.generateSavedData()
//        val invalidUserPayload: UserPayload = userPayloadTestDataGenerator.generateInvalidData()
//        val request: MockHttpServletRequestBuilder = patch(usersIdPath.format(savedUser.id))
//            .contentType(MediaType.APPLICATION_JSON)
//            .with(user(registeredAdmin))
//            .content(userPayloadJsonTester.write(invalidUserPayload).json)
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
//    fun updateUserByIdWithSameUsernameAndEmail() {
//        val savedUser: User = userTestDataGenerator.generateSavedData()
//        val userDataForUpdate: User = userTestDataGenerator.generateUnsavedData()
//        val userPayload: UserPayload = userDataForUpdate.toUserPayload().copy(
//            username = savedUser.username,
//            email = savedUser.email
//        )
//        val request: MockHttpServletRequestBuilder = patch(usersIdPath.format(savedUser.id))
//            .contentType(MediaType.APPLICATION_JSON)
//            .with(user(registeredAdmin))
//            .content(userPayloadJsonTester.write(userPayload).json)
//
//        mockMvc.perform(request)
//            .andExpectAll(
//                status().isOk,
//                jsonPath(ID).value(savedUser.id)
//            )
//            .andDo { result: MvcResult ->
//                val user: User = result.extract(userJsonTester)
//                assertTrue(user.isMatches(userPayload))
//            }
//    }
//
//    @Test
//    fun updateUserByNonExistentId() {
//        val unsavedUser: User = userTestDataGenerator.generateUnsavedData()
//        val userPayload: UserPayload = userPayloadTestDataGenerator.generateUnsavedData()
//        val request: MockHttpServletRequestBuilder = patch(usersIdPath.format(unsavedUser.id))
//            .contentType(MediaType.APPLICATION_JSON)
//            .with(user(registeredAdmin))
//            .content(userPayloadJsonTester.write(userPayload).json)
//
//        assertThrows(ServletException::class.java) {
//            mockMvc.perform(request)
//        }
//    }
//
//    @Test
//    fun updateUserByIdWithNotUniqueUsername() {
//        val savedUser: User = userTestDataGenerator.generateSavedData()
//        val anotherSavedUser: User = userTestDataGenerator.generateSavedData()
//        val userDataForUpdate: User = userTestDataGenerator.generateUnsavedData()
//        val userPayload: UserPayload = userDataForUpdate.toUserPayload().copy(
//            username = anotherSavedUser.username
//        )
//        val request: MockHttpServletRequestBuilder = patch(usersIdPath.format(savedUser.id))
//            .contentType(MediaType.APPLICATION_JSON)
//            .with(user(registeredAdmin))
//            .content(userPayloadJsonTester.write(userPayload).json)
//
//        mockMvc.perform(request)
//            .andExpectAll(
//                status().isConflict,
//                jsonPath(ERROR).value(DUPLICATE_FIELD),
//                jsonPath(FIELD).value(USERNAME)
//            )
//    }
//
//    @Test
//    fun updateUserByIdWithNotUniqueEmail() {
//        val savedUser: User = userTestDataGenerator.generateSavedData()
//        val anotherSavedUser: User = userTestDataGenerator.generateSavedData()
//        val userDataForUpdate: User = userTestDataGenerator.generateUnsavedData()
//        val userPayload: UserPayload = userDataForUpdate.toUserPayload().copy(
//            email = anotherSavedUser.email
//        )
//        val request: MockHttpServletRequestBuilder = patch(usersIdPath.format(savedUser.id))
//            .contentType(MediaType.APPLICATION_JSON)
//            .with(user(registeredAdmin))
//            .content(userPayloadJsonTester.write(userPayload).json)
//
//        mockMvc.perform(request)
//            .andExpectAll(
//                status().isConflict,
//                jsonPath(ERROR).value(DUPLICATE_FIELD),
//                jsonPath(FIELD).value(EMAIL)
//            )
//    }
//
//    @Test
//    fun deleteUserById() {
//        val savedUser: User = userTestDataGenerator.generateSavedData()
//        val request: MockHttpServletRequestBuilder = delete(usersIdPath.format(savedUser.id))
//            .contentType(MediaType.APPLICATION_JSON)
//            .with(user(registeredAdmin))
//
//        mockMvc.perform(request)
//            .andExpect(status().isNoContent)
//    }
//
//    @Test
//    fun deleteUserByIdViaTargetUser() {
//        val savedUser: User = authenticationDetailsProducer.produceRegisteredUserWithRawPassword(UserRole.ROLE_USER)
//        val targetUserDetails: UserDetails = userToUserDetailsConverter.convert(savedUser)!!
//        val request: MockHttpServletRequestBuilder = delete(usersIdPath.format(savedUser.id))
//            .contentType(MediaType.APPLICATION_JSON)
//            .with(user(targetUserDetails))
//
//        mockMvc.perform(request)
//            .andExpect(status().isNoContent)
//    }
//
//    @Test
//    fun deleteUserByIdWithoutAccess() {
//        val savedUser: User = userTestDataGenerator.generateSavedData()
//        val request: MockHttpServletRequestBuilder = delete(usersIdPath.format(savedUser.id))
//            .contentType(MediaType.APPLICATION_JSON)
//            .with(user(registeredUser))
//
//        mockMvc.perform(request)
//            .andExpect(status().isForbidden)
//    }
//
//    @Test
//    fun deleteUserByNonExistentId() {
//        val unsavedUser: User = userTestDataGenerator.generateUnsavedData()
//        val request: MockHttpServletRequestBuilder = delete(usersIdPath.format(unsavedUser.id))
//            .contentType(MediaType.APPLICATION_JSON)
//            .with(user(registeredAdmin))
//
//        mockMvc.perform(request)
//            .andExpect(status().isNoContent)
//    }
//
//    @Test
//    fun findUserById() {
//        val savedUser: User = userTestDataGenerator.generateSavedData()
//        val request: MockHttpServletRequestBuilder = get(usersIdPath.format(savedUser.id))
//            .contentType(MediaType.APPLICATION_JSON)
//            .with(user(registeredUser))
//
//        mockMvc.perform(request)
//            .andExpectAll(
//                status().isOk,
//                jsonPath(ID).value(savedUser.id)
//            )
//            .andDo { result: MvcResult ->
//                val user: User = result.extract(userJsonTester)
//                assertEquals(savedUser, user)
//            }
//    }
//
//    @Test
//    fun findUserByNonExistentId() {
//        val unsavedUser: User = userTestDataGenerator.generateUnsavedData()
//        val request: MockHttpServletRequestBuilder = get(usersIdPath.format(unsavedUser.id))
//            .contentType(MediaType.APPLICATION_JSON)
//            .with(user(registeredUser))
//
//        mockMvc.perform(request)
//            .andExpect(status().isNotFound)
//    }
//
//    @Test
//    fun findAuthorsByArticleId() {
//        val unsavedArticle: Article = articleTestDataGenerator.generateUnsavedData()
//        val authors: List<User> = userTestDataGenerator.generateSavedData(NUM_OF_TEST_REVIEWS)
//        val authorIds: List<Int> = authors.map { it.id!! }
//        val savedArticle: Article = articleService.create(unsavedArticle, authorIds)
//        val request: MockHttpServletRequestBuilder = get(usersAuthorshipIdPath.format(savedArticle.id))
//            .contentType(MediaType.APPLICATION_JSON)
//            .with(user(registeredAdmin))
//
//        mockMvc.perform(request)
//            .andExpectAll(
//                status().isOk,
//                jsonPath(PATH).isArray,
//                jsonPath(LENGTH).value(authors.size)
//            )
//            .andDo { result: MvcResult ->
//                val responseUsers: List<User> = result.extract(userListJsonTester)
//                assertTrue(authors.containsAll(responseUsers))
//                assertTrue(responseUsers.containsAll(authors))
//            }
//    }
//
//    @Test
//    fun findAuthorsByNonExistentArticleId() {
//        val unsavedArticle: Article = articleTestDataGenerator.generateUnsavedData()
//        val request: MockHttpServletRequestBuilder = get(usersAuthorshipIdPath.format(unsavedArticle.id))
//            .contentType(MediaType.APPLICATION_JSON)
//            .with(user(registeredAdmin))
//
//        mockMvc.perform(request)
//            .andExpect(status().isOk)
//    }
//
//    @Test
//    fun findAllUsers() {
//        cleanDb()
//        val registeredUser: User = authenticationDetailsProducer.produceRegisteredUserWithRawPassword(UserRole.ROLE_USER)
//        val userDetails: UserDetails = userToUserDetailsConverter.convert(registeredUser)!!
//        val allUsers: List<User> = userTestDataGenerator.generateSavedData(NUM_OF_TEST_REVIEWS) + registeredUser
//        val request: MockHttpServletRequestBuilder = get(usersPath)
//            .contentType(MediaType.APPLICATION_JSON)
//            .with(user(userDetails))
//
//        mockMvc.perform(request)
//            .andExpectAll(
//                status().isOk,
//                jsonPath(PATH).isArray,
//                jsonPath(LENGTH).value(allUsers.size)
//            )
//            .andDo { result: MvcResult ->
//                val responseUsers: List<User> = result.extract(userListJsonTester)
//                assertTrue(allUsers.containsAll(responseUsers))
//                assertTrue(responseUsers.containsAll(allUsers))
//            }
//    }
//}