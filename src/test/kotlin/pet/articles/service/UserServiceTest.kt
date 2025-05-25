package pet.articles.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.core.qualifier.named
import org.koin.test.KoinTest
import org.koin.test.inject
import pet.articles.model.dto.Article

import pet.articles.model.dto.User
import pet.articles.service.user.UserService
import pet.articles.tool.exception.DuplicateUserException
import pet.articles.tool.generator.TestDataGenerator
import pet.articles.tool.testing.extension.DBCleanupExtension
import pet.articles.tool.testing.extension.KoinConfigureTestExtension

import java.util.NoSuchElementException

@ExtendWith(DBCleanupExtension::class)
@ExtendWith(KoinConfigureTestExtension::class)
class UserServiceTest : KoinTest {

    companion object {
        const val NUM_OF_TEST_USERS = 10
    }


    private val userService: UserService by inject()

    private val userGenerator: TestDataGenerator<User> by inject(
        named("UserTestDataGenerator")
    )

    private val articleGenerator: TestDataGenerator<Article> by inject(
        named("ArticleTestDataGenerator")
    )

    @Test
    fun saveUser() {
        val userForSave: User = userGenerator.generateUnsavedData()

        val savedUser: User = userService.create(userForSave)

        val userForCheck: User? = userService.findById(savedUser.id!!)
        assertNotNull(userForCheck)
        assertEquals(savedUser, userForCheck)
    }

    @Test
    fun saveUserWithInvalidData() {
        val invalidUser: User = userGenerator.generateInvalidData()
        
        assertThrows(RuntimeException::class.java) {
            userService.create(invalidUser)
        }
    }

    @Test
    fun saveUserWithNotUniqueUsername() {
        val savedUser: User = userGenerator.generateSavedData()
        val unsavedUser: User = userGenerator.generateUnsavedData().copy(
            username = savedUser.username
        )

        assertThrows(DuplicateUserException::class.java) {
            userService.create(unsavedUser)
        }
    }

    @Test
    fun saveUserWithNotUniqueEmail() {
        val savedUser: User = userGenerator.generateSavedData()
        val unsavedUser: User = userGenerator.generateUnsavedData().copy(
            email = savedUser.email
        )

        assertThrows(DuplicateUserException::class.java) {
            userService.create(unsavedUser)
        }
    }

    @Test
    fun updateUserById() {
        val savedUser: User = userGenerator.generateSavedData()
        val userDataForUpdate: User = userGenerator.generateUnsavedData()

        val updatedUser: User = userService.updateById(userDataForUpdate, savedUser.id!!)

        val userForCheck: User? = userService.findById(savedUser.id!!)
        assertNotNull(userForCheck)
        assertEquals(updatedUser, userForCheck)
    }

    @Test
    fun updateUserByIdWithInvalidData() {
        val invalidUser: User = userGenerator.generateInvalidData()
        val savedUser: User = userGenerator.generateSavedData()

        assertThrows(RuntimeException::class.java) {
            userService.updateById(invalidUser, savedUser.id!!)
        }
    }

    @Test
    fun updateUserByIdWithSameUsernameAndEmail() {
        val savedUser: User = userGenerator.generateSavedData()
        val userDataForUpdate: User = userGenerator.generateUnsavedData().copy(
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
        val unsavedUser: User = userGenerator.generateUnsavedData()
        val userDataForUpdate: User = userGenerator.generateUnsavedData()

        assertThrows(NoSuchElementException::class.java) {
            userService.updateById(userDataForUpdate, unsavedUser.id!!)
        }
    }

    @Test
    fun updateUserByIdWithNotUniqueUsername() {
        val savedUser: User = userGenerator.generateSavedData()
        val userDataForUpdate: User = userGenerator.generateUnsavedData()
        val anotherSavedUser: User = userGenerator.generateSavedData()
        val updatedUser: User = userDataForUpdate.copy(
            username = anotherSavedUser.username
        )

        assertThrows(DuplicateUserException::class.java) {
            userService.updateById(updatedUser, savedUser.id!!)
        }
    }

    @Test
    fun updateUserByIdWithNotUniqueEmail() {
        val savedUser: User = userGenerator.generateSavedData()
        val newUserDataForUpdate: User = userGenerator.generateUnsavedData()
        val anotherSavedUser: User = userGenerator.generateSavedData()
        val updatedUser: User = newUserDataForUpdate.copy(
            email = anotherSavedUser.email
        )

        assertThrows(DuplicateUserException::class.java) {
            userService.updateById(updatedUser, savedUser.id!!)
        }
    }

    @Test
    fun deleteUserById() {
        val savedUser: User = userGenerator.generateSavedData()

        userService.deleteById(savedUser.id!!)

        assertFalse(userService.existsById(savedUser.id!!))
    }

    @Test
    fun deleteUserByNonExistentId() {
        val unsavedUser: User = userGenerator.generateUnsavedData()

        userService.deleteById(unsavedUser.id!!)

        assertFalse(userService.existsById(unsavedUser.id!!))
    }

    @Test
    fun findUserById() {
        val savedUser: User = userGenerator.generateSavedData()

        val userForCheck: User? = userService.findById(savedUser.id!!)

        assertNotNull(userForCheck)
        assertEquals(savedUser, userForCheck)
    }

    @Test
    fun findUserByNonExistentId() {
        val unsavedUser: User = userGenerator.generateUnsavedData()

        val userForCheck: User? = userService.findById(unsavedUser.id!!)

        assertNull(userForCheck)
    }

    @Test
    fun findUserByUsername() {
        val savedUser: User = userGenerator.generateSavedData()

        val userForCheck: User? = userService.findByUsername(savedUser.username)

        assertNotNull(userForCheck)
        assertEquals(savedUser, userForCheck)
    }

    @Test
    fun findUserByNonExistentUsername() {
        val unsavedUser: User = userGenerator.generateUnsavedData()

        val userForCheck: User? = userService.findByUsername(unsavedUser.username)

        assertNull(userForCheck)
    }

    @Test
    fun findAuthorsByArticleId() {
        val savedArticle: Article = articleGenerator.generateSavedData()
        val authors: List<User> = userService.findAll()

        val authorsForCheck: List<User> = userService.findAuthorsByArticleId(savedArticle.id!!)

        assertEquals(authors.size, authorsForCheck.size)
        assertTrue(authors.containsAll(authorsForCheck))
        assertTrue(authorsForCheck.containsAll(authors))
    }

    @Test
    fun findAuthorsByNonExistentArticleId() {
        val unsavedArticle: Article = articleGenerator.generateUnsavedData()

        val authors: List<User> = userService.findAuthorsByArticleId(unsavedArticle.id!!)

        assertTrue(authors.isEmpty())
    }

    @Test
    fun findAllUsers() {
        val allUsers: List<User> = userGenerator.generateSavedData(NUM_OF_TEST_USERS)

        val usersForCheck: List<User> = userService.findAll()

        assertEquals(allUsers.size, usersForCheck.size)
        assertTrue(allUsers.containsAll(usersForCheck))
        assertTrue(usersForCheck.containsAll(allUsers))
    }
}