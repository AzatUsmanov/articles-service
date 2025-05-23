//package pet.articles.test.controller
//
//import org.junit.jupiter.api.AfterEach
//import org.junit.jupiter.api.Assertions.assertTrue
//import org.junit.jupiter.api.Test
//
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.beans.factory.annotation.Value
//import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
//import org.springframework.boot.test.context.SpringBootTest
//import org.springframework.boot.test.json.JacksonTester
//import org.springframework.http.MediaType
//import org.springframework.test.annotation.DirtiesContext
//import org.springframework.test.web.servlet.MockMvc
//import org.springframework.test.web.servlet.MvcResult
//import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
//import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
//import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
//import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
//
//import pet.articles.controller.advice.ValidationError.ErrorTypes.DUPLICATE_FIELD
//import pet.articles.controller.advice.ValidationError.ErrorTypes.INVALID_FIELD
//import pet.articles.controller.advice.ValidationError.ResponseContentTypes.LIST_OF_FIELD_ERRORS
//import pet.articles.model.dto.User
//import pet.articles.model.dto.payload.RegistrationPayload
//import pet.articles.test.tool.db.DBCleaner
//import pet.articles.test.tool.generator.TestDataGenerator
//import pet.articles.test.controller.constant.ControllerTestConstants.Fields.EMAIL
//import pet.articles.test.controller.constant.ControllerTestConstants.Fields.USERNAME
//import pet.articles.test.controller.constant.ControllerTestConstants.JsonPaths.ERROR
//import pet.articles.test.controller.constant.ControllerTestConstants.JsonPaths.FIELD
//import pet.articles.test.controller.constant.ControllerTestConstants.JsonPaths.ID
//import pet.articles.test.tool.extension.extract
//import pet.articles.test.tool.extension.isMatches
//import pet.articles.test.tool.extension.toRegistrationPayload
//
//@SpringBootTest
//@AutoConfigureMockMvc
//@AutoConfigureJsonTesters
//@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
//class RegistrationControllerTest {
//
//    @Value("\${api.paths.registration}")
//    lateinit var registrationPath: String
//
//    @Autowired
//    lateinit var mockMvc: MockMvc
//
//    @Autowired
//    lateinit var dbCleaner: DBCleaner
//
//    @Autowired
//    lateinit var userJsonTester: JacksonTester<User>
//
//    @Autowired
//    lateinit var registrationPayloadJsonTester: JacksonTester<RegistrationPayload>
//
//    @Autowired
//    lateinit var registrationPayloadTestDataGenerator: TestDataGenerator<RegistrationPayload>
//
//    @Autowired
//    lateinit var userTestDataGenerator: TestDataGenerator<User>
//
//    @AfterEach
//    fun cleanDB() {
//        dbCleaner.cleanUp()
//    }
//
//    @Test
//    fun registerUser() {
//        val registrationPayload: RegistrationPayload = registrationPayloadTestDataGenerator.generateUnsavedData()
//        val request: MockHttpServletRequestBuilder = post(registrationPath)
//            .contentType(MediaType.APPLICATION_JSON)
//            .content(registrationPayloadJsonTester.write(registrationPayload).json)
//
//        mockMvc.perform(request)
//            .andExpectAll(
//                status().isCreated,
//                jsonPath(ID).isNumber
//            )
//            .andDo { result: MvcResult ->
//                val user: User = result.extract(userJsonTester)
//                assertTrue(user.isMatches(registrationPayload))
//            }
//    }
//
//    @Test
//    fun registerWithInvalidData() {
//        val invalidRegistrationPayload: RegistrationPayload = registrationPayloadTestDataGenerator.generateInvalidData()
//        val request: MockHttpServletRequestBuilder = post(registrationPath)
//            .contentType(MediaType.APPLICATION_JSON)
//            .content(registrationPayloadJsonTester.write(invalidRegistrationPayload).json)
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
//    fun registerUserWithNotUniqueUsername() {
//        val savedUser: User = userTestDataGenerator.generateSavedData()
//        val unsavedUser: User = userTestDataGenerator.generateUnsavedData()
//        val registrationPayload = unsavedUser.toRegistrationPayload().copy(
//            username = savedUser.username
//        )
//        val request: MockHttpServletRequestBuilder = post(registrationPath)
//            .contentType(MediaType.APPLICATION_JSON)
//            .content(registrationPayloadJsonTester.write(registrationPayload).json)
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
//    fun registerUserWithNotUniqueEmail() {
//        val savedUser: User = userTestDataGenerator.generateSavedData()
//        val unsavedUser: User = userTestDataGenerator.generateUnsavedData()
//        val registrationPayload = unsavedUser.toRegistrationPayload().copy(
//            email = savedUser.email
//        )
//        val request: MockHttpServletRequestBuilder = post(registrationPath)
//            .contentType(MediaType.APPLICATION_JSON)
//            .content(registrationPayloadJsonTester.write(registrationPayload).json)
//
//        mockMvc.perform(request)
//            .andExpectAll(
//                status().isConflict,
//                jsonPath(ERROR).value(DUPLICATE_FIELD),
//                jsonPath(FIELD).value(EMAIL)
//            )
//    }
//}