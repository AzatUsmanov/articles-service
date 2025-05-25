package pet.articles.web.routing

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.core.qualifier.named
import org.koin.test.KoinTest
import org.koin.test.inject
import pet.articles.model.dto.Article
import pet.articles.model.dto.ErrorResponse
import pet.articles.model.dto.User
import pet.articles.model.dto.payload.UserPayload
import pet.articles.model.enums.ErrorResponseType
import pet.articles.model.enums.UserRole
import pet.articles.service.article.ArticleService
import pet.articles.tool.extension.getProperty
import pet.articles.tool.extension.isMatches
import pet.articles.tool.extension.toUserPayload
import pet.articles.tool.generator.TestDataGenerator
import pet.articles.tool.testing.extension.DBCleanupExtension
import pet.articles.tool.testing.extension.KoinConfigureTestExtension
import pet.articles.web.testApplicationAsAuthorized
import pet.articles.web.testApplicationAsUnauthorized
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExtendWith(DBCleanupExtension::class)
@ExtendWith(KoinConfigureTestExtension::class)
class UserRoutingTest : KoinTest {

    companion object {
        const val NUM_OF_TEST_USERS = 10
    }

    private val usersPath = getProperty("api.paths.users")
    
    private val usersIdPath = "$usersPath/%d"

    private val usersAdminPath = getProperty("api.paths.users.admin")

    private val usersAuthorshipPath = "${getProperty("api.paths.users.authorship")}/%d"

    private val userGenerator: TestDataGenerator<User> by inject(
        named("UserTestDataGenerator")
    )

    private val userPayloadGenerator: TestDataGenerator<UserPayload> by inject(
        named("UserPayloadTestDataGenerator")
    )

    private val articleGenerator: TestDataGenerator<Article> by inject(
        named("ArticleTestDataGenerator")
    )

    private val articleService: ArticleService by inject()

    @Test
    fun createUser() = testApplicationAsAuthorized { client, _ ->
        val payload: UserPayload = userPayloadGenerator.generateUnsavedData()

        val response: HttpResponse = client.post(usersAdminPath) {
            setBody(payload)
        }

        val createdUser: User = response.body()
        assertEquals(HttpStatusCode.Created, response.status)
        assertTrue(createdUser.isMatches(payload))
    }

    @Test
    fun createUserAsUnauthorized() = testApplicationAsUnauthorized { client ->
        val payload: UserPayload = userPayloadGenerator.generateUnsavedData()

        val response: HttpResponse = client.post(usersAdminPath) {
            setBody(payload)
        }

        val errorResponse: ErrorResponse = response.body()
        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertEquals(ErrorResponseType.AUTHENTICATION, errorResponse.errorResponseType)
        assertEquals("Token is not valid or has expired", errorResponse.message)
    }

    @Test
    fun createUserWithoutAccess() =
        testApplicationAsAuthorized(UserRole.ROLE_USER) { client, authorizedUser ->
            val payload: UserPayload = userPayloadGenerator.generateUnsavedData()

            val response: HttpResponse = client.post(usersAdminPath) {
                setBody(payload)
            }

            val errorResponse: ErrorResponse = response.body()
            assertEquals(HttpStatusCode.Forbidden, response.status)
            assertEquals(ErrorResponseType.AUTHENTICATION, errorResponse.errorResponseType)
            assertEquals(
                "User with username = ${authorizedUser.username} does not have the required permissions",
                errorResponse.message
            )
        }

    @Test
    fun createUserWithInvalidData() = testApplicationAsAuthorized { client, _ ->
        val invalidPayload: UserPayload = userPayloadGenerator.generateInvalidData()

        val response: HttpResponse = client.post(usersAdminPath) {
            setBody(invalidPayload)
        }

        val errorResponse: ErrorResponse = response.body()
        assertEquals(HttpStatusCode.UnprocessableEntity, response.status)
        assertEquals(ErrorResponseType.VALIDATION, errorResponse.errorResponseType)
        assertTrue(errorResponse.message.startsWith("Validation failed for UserPayload"))
        assertEquals(
            mapOf("username" to "the length of the username must be between 5 and 30"),
            errorResponse.details
        )
    }

    @Test
    fun createUserWithNotUniqueUsername() = testApplicationAsAuthorized { client, _ ->
        val savedUser: User = userGenerator.generateSavedData()
        val payload: UserPayload = userGenerator
            .generateUnsavedData()
            .toUserPayload()
            .copy(username = savedUser.username)

        val response: HttpResponse = client.post(usersAdminPath) {
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
    fun createUserWithNotUniqueEmail() = testApplicationAsAuthorized { client, _ ->
        val savedUser: User = userGenerator.generateSavedData()
        val payload: UserPayload = userGenerator
            .generateUnsavedData()
            .toUserPayload()
            .copy(email = savedUser.email)

        val response: HttpResponse = client.post(usersAdminPath) {
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

    @Test
    fun updateUserById() = testApplicationAsAuthorized { client, _ ->
        val savedUser: User = userGenerator.generateSavedData()
        val payload: UserPayload = userPayloadGenerator.generateUnsavedData()

        val response: HttpResponse = client.patch(usersIdPath.format(savedUser.id!!)) {
            setBody(payload)
        }

        val updatedUser: User = response.body()
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(savedUser.id, updatedUser.id)
        assertTrue(updatedUser.isMatches(payload))
    }

    @Test
    fun updateUserByIdAsUnauthorized() = testApplicationAsUnauthorized { client ->
        val savedUser: User = userGenerator.generateSavedData()
        val payload: UserPayload = userPayloadGenerator.generateUnsavedData()

        val response: HttpResponse = client.patch(usersIdPath.format(savedUser.id!!)) {
            setBody(payload)
        }

        val errorResponse: ErrorResponse = response.body()
        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertEquals(ErrorResponseType.AUTHENTICATION, errorResponse.errorResponseType)
        assertEquals("Token is not valid or has expired", errorResponse.message)
    }

    @Test
    fun updateUserByIdViaTargetUser() =
        testApplicationAsAuthorized(UserRole.ROLE_USER) { client, authorizedUser ->
            val payload: UserPayload = userPayloadGenerator.generateUnsavedData()

            val response: HttpResponse = client.patch("$usersPath/${authorizedUser.id!!}") {
                setBody(payload)
            }

            val updatedUser: User = response.body()
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(authorizedUser.id, updatedUser.id)
            assertTrue(updatedUser.isMatches(payload))
        }

    @Test
    fun updateUserByIdWithoutAccess() =
        testApplicationAsAuthorized(UserRole.ROLE_USER) { client, authorizedUser ->
            val savedUser: User = userGenerator.generateSavedData()
            val payload: UserPayload = userPayloadGenerator.generateUnsavedData()

            val response: HttpResponse = client.patch(usersIdPath.format(savedUser.id!!)) {
                setBody(payload)
            }

            val errorResponse: ErrorResponse = response.body()
            assertEquals(HttpStatusCode.Forbidden, response.status)
            assertEquals(ErrorResponseType.AUTHENTICATION, errorResponse.errorResponseType)
            assertEquals(
                "User with username = ${authorizedUser.username} doesn't have the required permissions",
                errorResponse.message
            )
        }

    @Test
    fun updateUserByIdWithInvalidData() = testApplicationAsAuthorized { client, _ ->
        val savedUser: User = userGenerator.generateSavedData()
        val invalidPayload: UserPayload = userPayloadGenerator.generateInvalidData()

        val response: HttpResponse = client.patch(usersIdPath.format(savedUser.id!!)) {
            setBody(invalidPayload)
        }

        val errorResponse: ErrorResponse = response.body()
        assertEquals(HttpStatusCode.UnprocessableEntity, response.status)
        assertEquals(ErrorResponseType.VALIDATION, errorResponse.errorResponseType)
        assertTrue(errorResponse.message.startsWith("Validation failed for UserPayload"))
        assertEquals(
            mapOf("username" to "the length of the username must be between 5 and 30"),
            errorResponse.details
        )
    }

    @Test
    fun updateUserByIdWithSameUsernameAndEmail() = testApplicationAsAuthorized { client, _ ->
        val savedUser: User = userGenerator.generateSavedData()
        val payload: UserPayload = userGenerator
            .generateUnsavedData()
            .toUserPayload()
            .copy(
                username = savedUser.username,
                email = savedUser.email
            )

        val response: HttpResponse = client.patch(usersIdPath.format(savedUser.id!!)) {
            setBody(payload)
        }

        val updatedUser: User = response.body()
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(savedUser.id, updatedUser.id)
        assertTrue(updatedUser.isMatches(payload))
    }

    @Test
    fun updateUserByNonExistentId() = testApplicationAsAuthorized { client, _ ->
        val unsavedUser: User = userGenerator.generateUnsavedData()
        val payload: UserPayload = userPayloadGenerator.generateUnsavedData()

        val response: HttpResponse = client.patch("$usersPath/${unsavedUser.id!!}") {
            setBody(payload)
        }

        val errorResponse: ErrorResponse = response.body()
        assertEquals(HttpStatusCode.InternalServerError, response.status)
        assertEquals(
            "User with id ${unsavedUser.id!!} not found",
            errorResponse.message
        )
    }

    @Test
    fun updateUserByIdWithNotUniqueUsername() = testApplicationAsAuthorized { client, _ ->
        val savedUser: User = userGenerator.generateSavedData()
        val anotherSavedUser: User = userGenerator.generateSavedData()
        val payload: UserPayload = userGenerator.generateUnsavedData().toUserPayload()
            .copy(username = anotherSavedUser.username)

        val response: HttpResponse = client.patch(usersIdPath.format(savedUser.id!!)) {
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
    fun updateUserByIdWithNotUniqueEmail() = testApplicationAsAuthorized { client, _ ->
        val savedUser: User = userGenerator.generateSavedData()
        val anotherSavedUser: User = userGenerator.generateSavedData()
        val payload: UserPayload = userGenerator.generateUnsavedData().toUserPayload()
            .copy(email = anotherSavedUser.email)

        val response: HttpResponse = client.patch(usersIdPath.format(savedUser.id!!)) {
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

    @Test
    fun deleteUserById() = testApplicationAsAuthorized { client, _ ->
        val savedUser: User = userGenerator.generateSavedData()

        val response: HttpResponse = client.delete(usersIdPath.format(savedUser.id!!))

        assertEquals(HttpStatusCode.NoContent, response.status)
    }

    @Test
    fun deleteUserByIdAsUnauthorized() = testApplicationAsUnauthorized { client ->
        val savedUser: User = userGenerator.generateSavedData()

        val response: HttpResponse = client.delete(usersIdPath.format(savedUser.id!!))

        val errorResponse: ErrorResponse = response.body()
        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertEquals(ErrorResponseType.AUTHENTICATION, errorResponse.errorResponseType)
        assertEquals("Token is not valid or has expired", errorResponse.message)
    }

    @Test
    fun deleteUserByIdViaTargetUser() =
        testApplicationAsAuthorized(UserRole.ROLE_USER) { client, authorizedUser ->
        val response: HttpResponse = client.delete("$usersPath/${authorizedUser.id!!}")

        assertEquals(HttpStatusCode.NoContent, response.status)
    }

    @Test
    fun deleteUserByIdWithoutAccess() =
        testApplicationAsAuthorized(UserRole.ROLE_USER) { client, authorizedUser ->
            val savedUser: User = userGenerator.generateSavedData()

            val response: HttpResponse = client.delete(usersIdPath.format(savedUser.id!!))

            val errorResponse: ErrorResponse = response.body()
            assertEquals(HttpStatusCode.Forbidden, response.status)
            assertEquals(ErrorResponseType.AUTHENTICATION, errorResponse.errorResponseType)
            assertEquals(
                "User with username = ${authorizedUser.username} doesn't have the required permissions",
                errorResponse.message
            )
        }

    @Test
    fun deleteUserByNonExistentId() = testApplicationAsAuthorized { client, _ ->
        val unsavedUser: User = userGenerator.generateUnsavedData()

        val response: HttpResponse = client.delete("$usersPath/${unsavedUser.id!!}")

        assertEquals(HttpStatusCode.NoContent, response.status)
    }

    @Test
    fun findUserById() = testApplicationAsAuthorized { client, _ ->
        val savedUser: User = userGenerator.generateSavedData()

        val response: HttpResponse = client.get(usersIdPath.format(savedUser.id!!))

        val foundUser: User = response.body()
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(savedUser, foundUser)
    }

    @Test
    fun findUserByIdAsUnauthorized() = testApplicationAsUnauthorized { client ->
        val savedUser: User = userGenerator.generateSavedData()

        val response: HttpResponse = client.get(usersIdPath.format(savedUser.id!!))

        val errorResponse: ErrorResponse = response.body()
        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertEquals(ErrorResponseType.AUTHENTICATION, errorResponse.errorResponseType)
        assertEquals("Token is not valid or has expired", errorResponse.message)
    }

    @Test
    fun findUserByNonExistentId() = testApplicationAsAuthorized { client, _ ->
        val unsavedUser: User = userGenerator.generateUnsavedData()

        val response: HttpResponse = client.get(usersIdPath.format(unsavedUser.id!!))

        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun findAuthorsByArticleId() = testApplicationAsAuthorized { client, _ ->
        val authors: List<User> = userGenerator.generateSavedData(NUM_OF_TEST_USERS)
        val article: Article = articleGenerator.generateUnsavedData()
        val savedArticle: Article = articleService.create(article, authors.map { it.id!! })

        val response: HttpResponse = client.get(usersAuthorshipPath.format(savedArticle.id!!))

        val foundAuthors: List<User> = response.body()
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(authors.toSet(), foundAuthors.toSet())
    }

    @Test
    fun findAuthorsByArticleIdAsUnauthorized() = testApplicationAsUnauthorized { client ->
        val authors: List<User> = userGenerator.generateSavedData(NUM_OF_TEST_USERS)
        val article: Article = articleGenerator.generateUnsavedData()
        val savedArticle: Article = articleService.create(article, authors.map { it.id!! })

        val response: HttpResponse = client.get(usersAuthorshipPath.format(savedArticle.id!!))

        val errorResponse: ErrorResponse = response.body()
        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertEquals(ErrorResponseType.AUTHENTICATION, errorResponse.errorResponseType)
        assertEquals("Token is not valid or has expired", errorResponse.message)
    }


    @Test
    fun findAuthorsByNonExistentArticleId() = testApplicationAsAuthorized{ client, _ ->
        val unsavedArticle: Article = articleGenerator.generateUnsavedData()

        val response: HttpResponse = client.get(usersAuthorshipPath.format(unsavedArticle.id!!))

        val foundAuthors: List<User> = response.body()
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(foundAuthors.isEmpty())
    }

    @Test
    fun findAllUsers() = testApplicationAsAuthorized{ client, authorizedUser ->
        val allUsers: List<User> = userGenerator.generateSavedData(NUM_OF_TEST_USERS) + authorizedUser

        val response: HttpResponse = client.get(usersPath)

        val foundUsers: List<User> = response.body()
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(allUsers.toSet(), foundUsers.toSet())
    }

    @Test
    fun findAllUsersAsUnauthorized() = testApplicationAsUnauthorized { client ->
        val response: HttpResponse = client.get(usersPath)

        val errorResponse: ErrorResponse = response.body()
        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertEquals(ErrorResponseType.AUTHENTICATION, errorResponse.errorResponseType)
        assertEquals("Token is not valid or has expired", errorResponse.message)
    }
}
