package pet.articles.service

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.core.qualifier.named
import org.koin.test.KoinTest
import org.koin.test.inject
import org.koin.test.junit5.KoinTestExtension
import org.mindrot.jbcrypt.BCrypt

import pet.articles.model.dto.User
import pet.articles.service.RegistrationService
import pet.articles.service.UserService
import pet.articles.config.testConfigure
import pet.articles.tool.exception.DuplicateUserException
import pet.articles.tool.db.DBCleaner
import pet.articles.tool.generator.TestDataGenerator
import pet.articles.tool.testing.extension.DBCleanupExtension
import pet.articles.tool.testing.extension.KoinConfigureTestExtension

@ExtendWith(DBCleanupExtension::class)
@ExtendWith(KoinConfigureTestExtension::class)
class RegistrationServiceTest : KoinTest {

    private val registrationService: RegistrationService by inject()

    private val userService: UserService by inject()

    private val userGenerator: TestDataGenerator<User> by inject(
        named("UserTestDataGenerator")
    )

    @Test
    fun registerUser() {
        val userForRegistration: User = userGenerator.generateUnsavedData()
        val userForRegistrationRawPassword: String = userForRegistration.password!!
        val registeredUser: User = registrationService.register(userForRegistration)

        val userForCheck: User? = userService.findById(registeredUser.id!!)
        assertNotNull(userForCheck)
        assertEquals(registeredUser, userForCheck!!)
        assertTrue(
            BCrypt.checkpw(
                userForRegistrationRawPassword,
                userForCheck.password
            )
        )
    }

    @Test
    fun registerWithInvalidData() {
        val invalidUser: User = userGenerator.generateInvalidData()

        assertThrows(RuntimeException::class.java) {
            registrationService.register(invalidUser)
        }
    }

    @Test
    fun registerUserWithNotUniqueUsername() {
        val savedUser: User = userGenerator.generateSavedData()
        val userForRegistration: User = userGenerator.generateUnsavedData().copy(
            username = savedUser.username
        )

        assertThrows(DuplicateUserException::class.java) {
            registrationService.register(userForRegistration)
        }
    }

    @Test
    fun registerUserWithNotUniqueEmail() {
        val savedUser: User = userGenerator.generateSavedData()
        val userForRegistration: User = userGenerator.generateUnsavedData().copy (
            email = savedUser.email
        )

        assertThrows(DuplicateUserException::class.java) {
            registrationService.register(userForRegistration)
        }
    }
}