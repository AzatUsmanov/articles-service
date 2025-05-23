package pet.articles.test.service

import org.junit.jupiter.api.Assertions.assertThrows
import com.auth0.jwt.interfaces.JWTVerifier
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.core.qualifier.named
import org.koin.test.KoinTest
import org.koin.test.inject
import org.koin.test.junit5.KoinTestExtension

import pet.articles.model.dto.User
import pet.articles.model.dto.payload.AuthRequest
import pet.articles.model.dto.AuthResponse
import pet.articles.service.AuthService
import pet.articles.test.testConfigure
import pet.articles.test.tool.db.DBCleaner
import pet.articles.test.tool.extension.toAuthRequest
import pet.articles.test.tool.generator.TestDataGenerator
import pet.articles.test.tool.producer.AuthenticationDetailsProducer
import pet.articles.tool.exception.AuthenticationException
import pet.articles.tool.exception.BadCredentialsException
import pet.articles.web.config.buildVerifier

class AuthServiceTest : KoinTest {

    private val secret: String = "secret"

    private val issuer: String = "issuer"

    private val audience: String = "audience"

    private val jwtVerifier: JWTVerifier = buildVerifier(secret, audience, issuer)

    private val dbCleaner: DBCleaner by inject()
    
    private val authService: AuthService by inject()

    private val userGenerator: TestDataGenerator<User> by inject(
        named("UserTestDataGenerator")
    )

    private val authenticationDetailsProducer: AuthenticationDetailsProducer by inject()

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