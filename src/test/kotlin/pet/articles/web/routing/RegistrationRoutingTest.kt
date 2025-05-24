package pet.articles.web.routing

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.core.qualifier.named
import org.koin.test.KoinTest
import org.koin.test.inject
import org.koin.test.junit5.KoinTestExtension
import org.mindrot.jbcrypt.BCrypt
import pet.articles.model.dto.ErrorResponse
import pet.articles.model.dto.User
import pet.articles.model.dto.payload.RegistrationPayload
import pet.articles.model.enums.ErrorResponseType
import pet.articles.config.testConfigure
import pet.articles.tool.db.DBCleaner
import pet.articles.tool.extension.getProperty
import pet.articles.tool.extension.isMatches
import pet.articles.tool.extension.toRegistrationPayload
import pet.articles.tool.generator.TestDataGenerator
import pet.articles.tool.testing.extension.DBCleanupExtension
import pet.articles.tool.testing.extension.KoinConfigureTestExtension
import pet.articles.web.testApplicationAsUnauthorized
import kotlin.test.Test
import kotlin.test.assertEquals


@ExtendWith(DBCleanupExtension::class)
@ExtendWith(KoinConfigureTestExtension::class)
class RegistrationRoutingTest : KoinTest {

    private val path: String = getProperty("api.paths.registration")

    private val userGenerator: TestDataGenerator<User> by inject(
        named("UserTestDataGenerator")
    )

    private val registrationPayloadGenerator: TestDataGenerator<RegistrationPayload> by inject(
        named("RegistrationPayloadTestDataGenerator")
    )

    @Test
    fun registerUser() = testApplicationAsUnauthorized { client ->
        val payload: RegistrationPayload = registrationPayloadGenerator.generateUnsavedData()

        val response: HttpResponse = client.post(path) {
            setBody(payload)
        }

        val registeredUser: User = response.body()
        assertEquals(HttpStatusCode.Created, response.status)
        assertTrue(registeredUser.isMatches(payload))
        assertTrue(
            BCrypt.checkpw(
                payload.password,
                registeredUser.password
            )
        )
    }

    @Test
    fun registerWithInvalidData() = testApplicationAsUnauthorized { client ->
        val invalidPayload: RegistrationPayload = registrationPayloadGenerator.generateInvalidData()

        val response: HttpResponse = client.post(path) {
            setBody(invalidPayload)
        }

        val errorResponse: ErrorResponse = response.body()
        assertEquals(HttpStatusCode.UnprocessableEntity, response.status)
        assertEquals(ErrorResponseType.VALIDATION, errorResponse.errorResponseType)
        assertTrue(errorResponse.message.startsWith("Validation failed for RegistrationPayload"))
        assertEquals(
            mapOf("username" to "the length of the username must be between 5 and 30"),
            errorResponse.details
        )
    }

    @Test
    fun registerUserWithNotUniqueUsername() = testApplicationAsUnauthorized { client ->
        val savedUser = userGenerator.generateSavedData()
        val unsavedUser = userGenerator.generateUnsavedData()
        val payload = unsavedUser.toRegistrationPayload().copy(
            username = savedUser.username
        )

        val response: HttpResponse = client.post(path) {
            setBody(payload)
        }

        val errorResponse: ErrorResponse = response.body()
        assertEquals(HttpStatusCode.Conflict, response.status)
        assertEquals(ErrorResponseType.COMMON, errorResponse.errorResponseType)
        assertEquals(
            "User with username ${payload.username} already exists",
            errorResponse.message
        )
    }


    @Test
    fun registerUserWithNotUniqueEmail() = testApplicationAsUnauthorized { client ->
        val savedUser: User = userGenerator.generateSavedData()
        val unsavedUser: User = userGenerator.generateUnsavedData()
        val payload = unsavedUser.toRegistrationPayload().copy(
            email = savedUser.email
        )

        val response: HttpResponse = client.post(path) {
            setBody(payload)
        }

        val errorResponse: ErrorResponse = response.body()
        assertEquals(HttpStatusCode.Conflict, response.status)
        assertEquals(ErrorResponseType.COMMON, errorResponse.errorResponseType)
        assertEquals(
            "User with email ${payload.email} already exists",
            errorResponse.message
        )
    }
}



