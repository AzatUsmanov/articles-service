package pet.articles.test.service

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.core.qualifier.named
import org.koin.fileProperties
import org.koin.ksp.generated.module
import org.koin.ktor.plugin.Koin
import org.koin.test.KoinTest
import org.koin.test.get
import org.koin.test.inject
import org.koin.test.junit5.KoinTestExtension
import pet.articles.ArticlesServiceApplication
import pet.articles.config.DataSourceConfig
import pet.articles.config.FlywayConfig
import pet.articles.config.JooqConfig
import pet.articles.model.dto.Article

import pet.articles.model.dto.User
import pet.articles.model.dto.payload.UserPayload
import pet.articles.model.enums.UserRole
import pet.articles.service.UserService
import pet.articles.test.ArticlesServiceApplicationTests
import pet.articles.test.testConfigure
import pet.articles.tool.exception.DuplicateUserException
import pet.articles.test.tool.db.DBCleaner
import pet.articles.test.tool.generator.TestDataGenerator
import pet.articles.web.config.configureAuth
import pet.articles.web.config.configureExceptionHandling
import pet.articles.web.config.configureRouting
import pet.articles.web.config.configureSerialization
import pet.articles.web.config.configureValidation

import java.util.NoSuchElementException

class UserServiceTest : KoinTest {

    companion object {
        const val NUM_OF_TEST_USERS = 10
    }

    private val dbCleaner: DBCleaner by inject()

    private val userService: UserService by inject()

    private val userTestDataGenerator: TestDataGenerator<User> by inject(
        named("UserTestDataGenerator")
    )

    private val articleTestDataGenerator: TestDataGenerator<Article> by inject(
        named("ArticleTestDataGenerator")
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
    fun saveUser() {
        val userForSave: User = userTestDataGenerator.generateUnsavedData()

        val savedUser: User = userService.create(userForSave)

        val userForCheck: User? = userService.findById(savedUser.id!!)
        assertNotNull(userForCheck)
        assertEquals(savedUser, userForCheck)
    }

    @Test
    fun saveUserWithInvalidData() {
        val invalidUser: User = userTestDataGenerator.generateInvalidData()
        
        assertThrows(RuntimeException::class.java) {
            userService.create(invalidUser)
        }
    }

    @Test
    fun saveUserWithNotUniqueUsername() {
        val savedUser: User = userTestDataGenerator.generateSavedData()
        val unsavedUser: User = userTestDataGenerator.generateUnsavedData().copy(
            username = savedUser.username
        )

        assertThrows(DuplicateUserException::class.java) {
            userService.create(unsavedUser)
        }
    }

    @Test
    fun saveUserWithNotUniqueEmail() {
        val savedUser: User = userTestDataGenerator.generateSavedData()
        val unsavedUser: User = userTestDataGenerator.generateUnsavedData().copy(
            email = savedUser.email
        )

        assertThrows(DuplicateUserException::class.java) {
            userService.create(unsavedUser)
        }
    }

    @Test
    fun updateUserById() {
        val savedUser: User = userTestDataGenerator.generateSavedData()
        val userDataForUpdate: User = userTestDataGenerator.generateUnsavedData()

        val updatedUser: User = userService.updateById(userDataForUpdate, savedUser.id!!)

        val userForCheck: User? = userService.findById(savedUser.id!!)
        assertNotNull(userForCheck)
        assertEquals(updatedUser, userForCheck)
    }

    @Test
    fun updateUserByIdWithInvalidData() {
        val invalidUser: User = userTestDataGenerator.generateInvalidData()
        val savedUser: User = userTestDataGenerator.generateSavedData()

        assertThrows(RuntimeException::class.java) {
            userService.updateById(invalidUser, savedUser.id!!)
        }
    }

    @Test
    fun updateUserByIdWithSameUsernameAndEmail() {
        val savedUser: User = userTestDataGenerator.generateSavedData()
        val userDataForUpdate: User = userTestDataGenerator.generateUnsavedData().copy(
            username = savedUser.username,
            email = savedUser.email
        )

        val updatedUser: User = userService.updateById(userDataForUpdate, savedUser.id!!)

        val userForCheck: User? = userService.findById(savedUser.id!!)
        assertNotNull(userForCheck)
        assertEquals(updatedUser, userForCheck)
    }

    @Test
    fun updateUserByNonExistentId() {
        val unsavedUser: User = userTestDataGenerator.generateUnsavedData()
        val userDataForUpdate: User = userTestDataGenerator.generateUnsavedData()

        assertThrows(NoSuchElementException::class.java) {
            userService.updateById(userDataForUpdate, unsavedUser.id!!)
        }
    }

    @Test
    fun updateUserByIdWithNotUniqueUsername() {
        val savedUser: User = userTestDataGenerator.generateSavedData()
        val userDataForUpdate: User = userTestDataGenerator.generateUnsavedData()
        val anotherSavedUser: User = userTestDataGenerator.generateSavedData()
        val updatedUser: User = userDataForUpdate.copy(
            username = anotherSavedUser.username
        )

        assertThrows(DuplicateUserException::class.java) {
            userService.updateById(updatedUser, savedUser.id!!)
        }
    }

    @Test
    fun updateUserByIdWithNotUniqueEmail() {
        val savedUser: User = userTestDataGenerator.generateSavedData()
        val newUserDataForUpdate: User = userTestDataGenerator.generateUnsavedData()
        val anotherSavedUser: User = userTestDataGenerator.generateSavedData()
        val updatedUser: User = newUserDataForUpdate.copy(
            email = anotherSavedUser.email
        )

        assertThrows(DuplicateUserException::class.java) {
            userService.updateById(updatedUser, savedUser.id!!)
        }
    }

    @Test
    fun deleteUserById() {
        val savedUser: User = userTestDataGenerator.generateSavedData()

        userService.deleteById(savedUser.id!!)

        assertFalse(userService.existsById(savedUser.id!!))
    }

    @Test
    fun deleteUserByNonExistentId() {
        val unsavedUser: User = userTestDataGenerator.generateUnsavedData()

        userService.deleteById(unsavedUser.id!!)

        assertFalse(userService.existsById(unsavedUser.id!!))
    }

    @Test
    fun findUserById() {
        val savedUser: User = userTestDataGenerator.generateSavedData()

        val userForCheck: User? = userService.findById(savedUser.id!!)

        assertNotNull(userForCheck)
        assertEquals(savedUser, userForCheck)
    }

    @Test
    fun findUserByNonExistentId() {
        val unsavedUser: User = userTestDataGenerator.generateUnsavedData()

        val userForCheck: User? = userService.findById(unsavedUser.id!!)

        assertNull(userForCheck)
    }

    @Test
    fun findUserByUsername() {
        val savedUser: User = userTestDataGenerator.generateSavedData()

        val userForCheck: User? = userService.findByUsername(savedUser.username)

        assertNotNull(userForCheck)
        assertEquals(savedUser, userForCheck)
    }

    @Test
    fun findUserByNonExistentUsername() {
        val unsavedUser: User = userTestDataGenerator.generateUnsavedData()

        val userForCheck: User? = userService.findByUsername(unsavedUser.username)

        assertNull(userForCheck)
    }

    @Test
    fun findAuthorsByArticleId() {
        val savedArticle: Article = articleTestDataGenerator.generateSavedData()
        val authors: List<User> = userService.findAll()

        val authorsForCheck: List<User> = userService.findAuthorsByArticleId(savedArticle.id!!)

        assertEquals(authors.size, authorsForCheck.size)
        assertTrue(authors.containsAll(authorsForCheck))
        assertTrue(authorsForCheck.containsAll(authors))
    }

    @Test
    fun findAuthorsByNonExistentArticleId() {
        val unsavedArticle: Article = articleTestDataGenerator.generateUnsavedData()

        val authors: List<User> = userService.findAuthorsByArticleId(unsavedArticle.id!!)

        assertTrue(authors.isEmpty())
    }

    @Test
    fun findAllUsers() {
        val allUsers: List<User> = userTestDataGenerator.generateSavedData(NUM_OF_TEST_USERS)

        val usersForCheck: List<User> = userService.findAll()

        assertEquals(allUsers.size, usersForCheck.size)
        assertTrue(allUsers.containsAll(usersForCheck))
        assertTrue(usersForCheck.containsAll(allUsers))
    }
}