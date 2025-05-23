package pet.articles.test.service

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.core.qualifier.named
import org.koin.fileProperties
import org.koin.ksp.generated.module
import org.koin.test.KoinTest
import org.koin.test.inject
import org.koin.test.junit5.KoinTestExtension
import org.mindrot.jbcrypt.BCrypt
import pet.articles.ArticlesServiceApplication
import pet.articles.config.DataSourceConfig
import pet.articles.config.FlywayConfig
import pet.articles.config.JooqConfig

import pet.articles.model.dto.User
import pet.articles.service.RegistrationService
import pet.articles.service.UserService
import pet.articles.test.ArticlesServiceApplicationTests
import pet.articles.test.testConfigure
import pet.articles.tool.exception.DuplicateUserException
import pet.articles.test.tool.db.DBCleaner
import pet.articles.test.tool.generator.TestDataGenerator

class RegistrationServiceTest : KoinTest {
    
    private val dbCleaner: DBCleaner by inject()
    
    private val registrationService: RegistrationService by inject()

    private val userService: UserService by inject()

    private val userTestDataGenerator: TestDataGenerator<User> by inject(
        named("UserTestDataGenerator")
    )

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
    fun registerUser() {
        val userForRegistration: User = userTestDataGenerator.generateUnsavedData()
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
        val invalidUser: User = userTestDataGenerator.generateInvalidData()

        assertThrows(RuntimeException::class.java) {
            registrationService.register(invalidUser)
        }
    }

    @Test
    fun registerUserWithNotUniqueUsername() {
        val savedUser: User = userTestDataGenerator.generateSavedData()
        val userForRegistration: User = userTestDataGenerator.generateUnsavedData().copy(
            username = savedUser.username
        )

        assertThrows(DuplicateUserException::class.java) {
            registrationService.register(userForRegistration)
        }
    }

    @Test
    fun registerUserWithNotUniqueEmail() {
        val savedUser: User = userTestDataGenerator.generateSavedData()
        val userForRegistration: User = userTestDataGenerator.generateUnsavedData().copy (
            email = savedUser.email
        )

        assertThrows(DuplicateUserException::class.java) {
            registrationService.register(userForRegistration)
        }
    }
}