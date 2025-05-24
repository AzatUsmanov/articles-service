package pet.articles.service

import org.junit.jupiter.api.Assertions.assertThrows
import com.auth0.jwt.interfaces.JWTVerifier
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import org.koin.test.KoinTest
import org.koin.test.inject
import org.koin.test.junit5.KoinTestExtension

import pet.articles.model.dto.User
import pet.articles.model.dto.payload.AuthRequest
import pet.articles.model.dto.AuthResponse
import pet.articles.service.AuthService
import pet.articles.config.testConfigure
import pet.articles.tool.db.DBCleaner
import pet.articles.tool.extension.toAuthRequest
import pet.articles.tool.generator.TestDataGenerator
import pet.articles.tool.producer.AuthenticationDetailsProducer
import pet.articles.tool.exception.AuthenticationException
import pet.articles.tool.exception.BadCredentialsException
import pet.articles.tool.testing.extension.DBCleanupExtension
import pet.articles.tool.testing.extension.KoinConfigureTestExtension

@ExtendWith(DBCleanupExtension::class)
@ExtendWith(KoinConfigureTestExtension::class)
class AuthServiceTest : KoinTest {

    private val jwtVerifier: JWTVerifier by inject()

    private val authService: AuthService by inject()

    private val userGenerator: TestDataGenerator<User> by inject(
        named("UserTestDataGenerator")
    )

    private val authenticationDetailsProducer: AuthenticationDetailsProducer by inject()

    @Test
    fun authenticateUser() {
        val authRequest = authenticationDetailsProducer.produceRegisteredUserWithRawPassword()
            .toAuthRequest()

        val response: AuthResponse = authService.authenticate(authRequest)

        jwtVerifier.verify(response.token)
    }

    @Test
    fun authenticateNonExistentUser() {
        val authRequest: AuthRequest = userGenerator.generateUnsavedData()
            .toAuthRequest()

        assertThrows(AuthenticationException::class.java) {
            authService.authenticate(authRequest)
        }
    }

    @Test
    fun authenticateWithWrongPassword() {
        val authRequest: AuthRequest = authenticationDetailsProducer.produceRegisteredUser()
            .copy(password = userGenerator.generateUnsavedData().password)
            .toAuthRequest()

        assertThrows(BadCredentialsException::class.java) {
            authService.authenticate(authRequest)
        }
    }
}