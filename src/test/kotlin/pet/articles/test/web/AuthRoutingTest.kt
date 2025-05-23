package pet.articles.test.web

import com.auth0.jwt.interfaces.JWTVerifier
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.core.qualifier.named
import org.koin.test.KoinTest
import org.koin.test.inject
import org.koin.test.junit5.KoinTestExtension
import pet.articles.model.dto.User
import pet.articles.model.dto.payload.AuthRequest
import pet.articles.model.dto.AuthResponse
import pet.articles.model.dto.ErrorResponse
import pet.articles.model.enums.ErrorResponseType
import pet.articles.test.testConfigure
import pet.articles.test.tool.db.DBCleaner
import pet.articles.test.tool.extension.toAuthRequest
import pet.articles.test.tool.generator.TestDataGenerator
import pet.articles.test.tool.producer.AuthenticationDetailsProducer
import pet.articles.web.config.buildVerifier
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuthRoutingTest : KoinTest {

    private val secret: String = "secret"

    private val issuer: String = "issuer"

    private val audience: String = "audience"

    private val authPath = "/api/auth"

    private val dbCleaner: DBCleaner by inject()

    private val userTestDataGenerator: TestDataGenerator<User> by inject(
        named("UserTestDataGenerator")
    )

    private val authenticationDetailsProducer: AuthenticationDetailsProducer by inject()

    private val jwtVerifier: JWTVerifier = buildVerifier(secret, audience, issuer)
    @JvmField
    @RegisterExtension
    val koinTestExtension = KoinTestExtension.create {
        testConfigure()
    }

    @AfterEach
    fun cleanDb() {
        dbCleaner.cleanUp()
    }

    @Test
    fun authenticateUser() = testApplicationAsUnauthorized { client ->
        val request: AuthRequest = authenticationDetailsProducer
            .produceRegisteredUserWithRawPassword()
            .toAuthRequest()

        val response: HttpResponse = client.post(authPath) {
            setBody(request)
        }

        val authResponse: AuthResponse = response.body()
        assertEquals(HttpStatusCode.OK, response.status)
        jwtVerifier.verify(authResponse.token)
    }

    @Test
    fun authenticateWithInvalidAuthRequest() = testApplicationAsUnauthorized { client ->
        val authRequest: AuthRequest = userTestDataGenerator
            .generateInvalidData()
            .toAuthRequest()

        val response: HttpResponse = client.post(authPath) {
            setBody(authRequest)
        }

        val errorResponse: ErrorResponse = response.body()
        assertEquals(HttpStatusCode.UnprocessableEntity, response.status)
        assertEquals(ErrorResponseType.VALIDATION, errorResponse.errorResponseType)
        assertTrue(errorResponse.message.startsWith("Validation failed for AuthRequest"))
        assertEquals(
            mapOf("username" to "the length of the username must be between 5 and 30"),
            errorResponse.details
        )
    }

    @Test
    fun authenticateNonExistentUser() = testApplicationAsUnauthorized { client ->
        val authRequest: AuthRequest = userTestDataGenerator
            .generateUnsavedData()
            .toAuthRequest()

        val response: HttpResponse = client.post(authPath) {
            setBody(authRequest)
        }

        val errorResponse: ErrorResponse = response.body()
        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertEquals(ErrorResponseType.AUTHENTICATION, errorResponse.errorResponseType)
        assertEquals(
            "User with username = ${authRequest.username} not found",
            errorResponse.message
        )
    }

    @Test
    fun authenticateWithWrongPassword() = testApplicationAsUnauthorized { client ->
        val authRequest: AuthRequest = authenticationDetailsProducer
            .produceRegisteredUser()
            .copy(password = userTestDataGenerator.generateUnsavedData().password)
            .toAuthRequest()

        val response: HttpResponse = client.post(authPath) {
            setBody(authRequest)
        }

        val errorResponse: ErrorResponse = response.body()
        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertEquals(ErrorResponseType.AUTHENTICATION, errorResponse.errorResponseType)
        assertEquals("Invalid password", errorResponse.message)
    }
}