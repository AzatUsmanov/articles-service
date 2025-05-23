package pet.articles.test.web

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.core.Koin
import org.koin.core.context.loadKoinModules
import org.koin.core.qualifier.named
import org.koin.fileProperties
import org.koin.ksp.generated.module
import org.koin.ktor.plugin.Koin
import org.koin.test.KoinTest
import org.koin.test.inject
import org.koin.test.junit5.KoinTestExtension
import pet.articles.ArticlesServiceApplication
import pet.articles.config.DataSourceConfig
import pet.articles.config.FlywayConfig
import pet.articles.config.JooqConfig
import pet.articles.model.dto.Article
import pet.articles.model.dto.ErrorResponse
import pet.articles.model.dto.Review
import pet.articles.model.dto.User
import pet.articles.model.dto.payload.NewArticlePayload
import pet.articles.model.dto.payload.RegistrationPayload
import pet.articles.model.dto.payload.ReviewPayload
import pet.articles.model.dto.payload.UpdateArticlePayload
import pet.articles.model.dto.payload.UserPayload
import pet.articles.model.enums.ErrorResponseType
import pet.articles.model.enums.UserRole
import pet.articles.service.ArticleService
import pet.articles.service.ReviewService
import pet.articles.service.ReviewServiceImpl
import pet.articles.service.UserService
import pet.articles.test.ArticlesServiceApplicationTests
import pet.articles.test.testConfigure
import pet.articles.test.tool.db.DBCleaner
import pet.articles.test.tool.extension.isMatches
import pet.articles.test.tool.extension.toNewArticlePayload
import pet.articles.test.tool.extension.toReviewPayload
import pet.articles.test.tool.extension.toUpdateArticlePayload
import pet.articles.test.tool.extension.toUserPayload
import pet.articles.test.tool.generator.TestDataGenerator
import pet.articles.web.config.configureAuth
import pet.articles.web.config.configureDoubleReceive
import pet.articles.web.config.configureExceptionHandling
import pet.articles.web.config.configureRouting
import pet.articles.web.config.configureSerialization
import pet.articles.web.config.configureValidation
import pet.articles.web.config.module
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class ArticleRoutingTest : KoinTest {

    private val articlesPath = "/api/articles"

    private val articlesIdPath = "$articlesPath/%d"

    private val articlesAuthorshipPath = "$articlesPath/authorship/%d"

    private val dbCleaner: DBCleaner by inject()

    private val articleService: ArticleService by inject()

    private val articleTestDataGenerator: TestDataGenerator<Article> by inject(
        named("ArticleTestDataGenerator")
    )

    private val newArticlePayloadTestDataGenerator: TestDataGenerator<NewArticlePayload> by inject(
        named("NewArticlePayloadTestDataGenerator")
    )

    private val updateArticlePayloadTestDataGenerator: TestDataGenerator<UpdateArticlePayload> by inject(
        named("UpdateArticlePayloadTestDataGenerator")
    )

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

    companion object {
        const val NUM_OF_TEST_ARTICLES = 10
    }

    @Test
    fun createArticle() = testApplicationAsAuthorized { client, _ ->
        val payload: NewArticlePayload = newArticlePayloadTestDataGenerator.generateUnsavedData()

        val response: HttpResponse = client.post(articlesPath) {
            setBody(payload)
        }

        val createdArticle: Article = response.body()
        assertEquals(HttpStatusCode.Created, response.status)
        assertTrue(createdArticle.isMatches(payload))
    }

    @Test
    fun createArticleAsUnauthorized() = testApplicationAsUnauthorized { client ->
        val payload: NewArticlePayload = newArticlePayloadTestDataGenerator.generateUnsavedData()

        val response: HttpResponse = client.post(articlesPath) {
            setBody(payload)
        }

        val errorResponse: ErrorResponse = response.body()
        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertEquals(ErrorResponseType.AUTHENTICATION, errorResponse.errorResponseType)
        assertEquals("Token is not valid or has expired", errorResponse.message)
    }

    @Test
    fun createArticleViaTargetUser() =
        testApplicationAsAuthorized(UserRole.ROLE_USER) { client, authorizedUser ->
            val payload: NewArticlePayload = newArticlePayloadTestDataGenerator
                .generateUnsavedData()
                .copy(authorIds = listOf(authorizedUser.id!!))

            val response: HttpResponse = client.post(articlesPath) {
                setBody(payload)
            }

            val createdArticle: Article = response.body()
            assertEquals(HttpStatusCode.Created, response.status)
            assertTrue(createdArticle.isMatches(payload))
        }

    @Test
    fun createArticleWithoutAccess() =
        testApplicationAsAuthorized(UserRole.ROLE_USER) { client, authorizedUser ->
            val payload: NewArticlePayload = newArticlePayloadTestDataGenerator.generateUnsavedData()

            val response: HttpResponse = client.post(articlesPath) {
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
    fun createArticleWithInvalidData() = testApplicationAsAuthorized { client, _ ->
        val invalidPayload: NewArticlePayload = newArticlePayloadTestDataGenerator.generateInvalidData()

        val response: HttpResponse = client.post(articlesPath) {
            setBody(invalidPayload)
        }

        val errorResponse: ErrorResponse = response.body()
        assertEquals(HttpStatusCode.UnprocessableEntity, response.status)
        assertEquals(ErrorResponseType.VALIDATION, errorResponse.errorResponseType)
        assertTrue(errorResponse.message.startsWith("Validation failed for NewArticlePayload"))
        assertEquals(
            mapOf("topic" to "the length of the topic must be between 1 and 50"),
            errorResponse.details
        )
    }


    @Test
    fun updateArticleById() = testApplicationAsAuthorized { client, _ ->
        val savedArticle: Article = articleTestDataGenerator.generateSavedData()
        val payload = savedArticle.toUpdateArticlePayload()

        val response: HttpResponse = client.patch(articlesIdPath.format(savedArticle.id)) {
            setBody(payload)
        }

        val updatedArticle: Article = response.body()
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(savedArticle.id, updatedArticle.id)
        assertTrue(updatedArticle.isMatches(payload))
    }

    @Test
    fun updateArticleByIdAsUnauthorized() = testApplicationAsUnauthorized { client ->
        val savedArticle: Article = articleTestDataGenerator.generateSavedData()
        val payload = savedArticle.toUpdateArticlePayload()

        val response: HttpResponse = client.patch(articlesIdPath.format(savedArticle.id)) {
            setBody(payload)
        }

        val errorResponse: ErrorResponse = response.body()
        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertEquals(ErrorResponseType.AUTHENTICATION, errorResponse.errorResponseType)
        assertEquals("Token is not valid or has expired", errorResponse.message)
    }

    @Test
    fun updateArticleByIdViaTargetUser() =
        testApplicationAsAuthorized(UserRole.ROLE_USER) { client, authorizedUser ->
            val savedArticle: Article = articleTestDataGenerator
                .generateUnsavedData()
                .let { article -> articleService.create(article, listOf(authorizedUser.id!!)) }
            val payload: UpdateArticlePayload = savedArticle.toUpdateArticlePayload()

            val response: HttpResponse = client.patch(articlesIdPath.format(savedArticle.id)) {
                setBody(payload)
            }

            val updatedArticle: Article = response.body()
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(savedArticle.id, updatedArticle.id)
            assertTrue(updatedArticle.isMatches(payload))
        }

    @Test
    fun updateArticleByIdWithoutAccess() =
            testApplicationAsAuthorized(UserRole.ROLE_USER) { client, authorizedUser ->
            val savedArticle: Article = articleTestDataGenerator.generateSavedData()
            val payload: UpdateArticlePayload = updateArticlePayloadTestDataGenerator.generateUnsavedData()

            val response: HttpResponse = client.patch(articlesIdPath.format(savedArticle.id)) {
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
    fun updateArticleByNonExistentId() = testApplicationAsAuthorized { client, _ ->
        val unsavedArticle: Article = articleTestDataGenerator.generateUnsavedData()
        val payload: UpdateArticlePayload = updateArticlePayloadTestDataGenerator.generateUnsavedData()

        val response: HttpResponse = client.patch(articlesIdPath.format(unsavedArticle.id!!)) {
            setBody(payload)
        }

        val errorResponse: ErrorResponse = response.body()
        assertEquals(HttpStatusCode.InternalServerError, response.status)
        assertEquals(ErrorResponseType.COMMON, errorResponse.errorResponseType)
        assertEquals(
            "Attempt to update article by non existent id = ${unsavedArticle.id}",
            errorResponse.message
        )
    }

    @Test
    fun updateArticleByIdWithInvalidData() = testApplicationAsAuthorized { client, _ ->
        val savedArticle: Article = articleTestDataGenerator.generateSavedData()
        val invalidPayload: UpdateArticlePayload = updateArticlePayloadTestDataGenerator.generateInvalidData()

        val response: HttpResponse = client.patch(articlesIdPath.format(savedArticle.id)) {
            setBody(invalidPayload)
        }

        val errorResponse: ErrorResponse = response.body()
        assertEquals(HttpStatusCode.UnprocessableEntity, response.status)
        assertEquals(ErrorResponseType.VALIDATION, errorResponse.errorResponseType)
        assertTrue(errorResponse.message.startsWith("Validation failed for UpdateArticlePayload"))
        assertEquals(
            mapOf("topic" to "the length of the topic must be between 1 and 50"),
            errorResponse.details
        )
    }

    @Test
    fun deleteArticleById() = testApplicationAsAuthorized { client, _ ->
        val savedArticle: Article = articleTestDataGenerator.generateSavedData()

        val response: HttpResponse = client.delete(articlesIdPath.format(savedArticle.id))

        assertEquals(HttpStatusCode.NoContent, response.status)
    }

    @Test
    fun deleteArticleByIdAsUnauthorized() = testApplicationAsUnauthorized { client ->
        val savedArticle: Article = articleTestDataGenerator.generateSavedData()

        val response: HttpResponse = client.delete(articlesIdPath.format(savedArticle.id))

        val errorResponse: ErrorResponse = response.body()
        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertEquals(ErrorResponseType.AUTHENTICATION, errorResponse.errorResponseType)
        assertEquals("Token is not valid or has expired", errorResponse.message)
    }

    @Test
    fun deleteArticleByIdViaTargetUser() =
        testApplicationAsAuthorized(UserRole.ROLE_USER) { client, authorizedUser ->
            val savedArticle: Article = articleTestDataGenerator
                .generateUnsavedData()
                .let { article -> articleService.create(article, listOf(authorizedUser.id!!)) }

            val response: HttpResponse = client.delete(articlesIdPath.format(savedArticle.id))

            assertEquals(HttpStatusCode.NoContent, response.status)
        }

    @Test
    fun deleteArticleByIdWithoutAccess() =
        testApplicationAsAuthorized(UserRole.ROLE_USER) { client, authorizedUser ->
            val savedArticle: Article = articleTestDataGenerator.generateSavedData()

            val response: HttpResponse = client.delete(articlesIdPath.format(savedArticle.id))

            val errorResponse: ErrorResponse = response.body()
            assertEquals(HttpStatusCode.Forbidden, response.status)
            assertEquals(ErrorResponseType.AUTHENTICATION, errorResponse.errorResponseType)
            assertEquals(
                "User with username = ${authorizedUser.username} doesn't have the required permissions",
                errorResponse.message
            )
        }

    @Test
    fun deleteArticleByNonExistentId() = testApplicationAsAuthorized { client, _ ->
        val unsavedArticle: Article = articleTestDataGenerator.generateUnsavedData()

        val response: HttpResponse = client.delete(articlesIdPath.format(unsavedArticle.id))

        assertEquals(HttpStatusCode.NoContent, response.status)
    }

    @Test
    fun findArticleById() = testApplicationAsAuthorized { client, _ ->
        val savedArticle: Article = articleTestDataGenerator.generateSavedData()

        val response: HttpResponse = client.get(articlesIdPath.format(savedArticle.id))

        val foundArticle: Article = response.body()
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(savedArticle, foundArticle)
    }

    @Test
    fun findArticleByIdAsUnauthorized() = testApplicationAsUnauthorized { client ->
        val savedArticle: Article = articleTestDataGenerator.generateSavedData()

        val response: HttpResponse = client.get(articlesIdPath.format(savedArticle.id))

        val errorResponse: ErrorResponse = response.body()
        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertEquals(ErrorResponseType.AUTHENTICATION, errorResponse.errorResponseType)
        assertEquals("Token is not valid or has expired", errorResponse.message)
    }

    @Test
    fun findArticleByNonExistentId() = testApplicationAsAuthorized { client, _ ->
        val unsavedArticle: Article = articleTestDataGenerator.generateUnsavedData()

        val response: HttpResponse = client.get(articlesIdPath.format(unsavedArticle.id))

        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun findArticlesByAuthorId() = testApplicationAsAuthorized { client, _ ->
        val savedAuthor: User = userTestDataGenerator.generateSavedData()
        val savedArticles: List<Article> = articleTestDataGenerator
            .generateUnsavedData(NUM_OF_TEST_ARTICLES)
            .map { articleService.create(it, listOf(savedAuthor.id!!)) }

        val response: HttpResponse = client.get(articlesAuthorshipPath.format(savedAuthor.id))

        val foundArticles: List<Article> = response.body()
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(savedArticles.toSet(), foundArticles.toSet())
    }

    @Test
    fun indArticlesByAuthorIdAsUnauthorized() = testApplicationAsUnauthorized { client ->
        val savedAuthor: User = userTestDataGenerator.generateSavedData()
        val savedArticles: List<Article> = articleTestDataGenerator
            .generateUnsavedData(NUM_OF_TEST_ARTICLES)
            .map { articleService.create(it, listOf(savedAuthor.id!!)) }

        val response: HttpResponse = client.get(articlesAuthorshipPath.format(savedAuthor.id))

        val errorResponse: ErrorResponse = response.body()
        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertEquals(ErrorResponseType.AUTHENTICATION, errorResponse.errorResponseType)
        assertEquals("Token is not valid or has expired", errorResponse.message)
    }

    @Test
    fun findArticlesByNonExistentAuthorId() = testApplicationAsAuthorized { client, _ ->
        val unsavedAuthor: User = userTestDataGenerator.generateUnsavedData()

        val response: HttpResponse = client.get(articlesAuthorshipPath.format(unsavedAuthor.id))

        val foundArticles: List<Article> = response.body()
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(foundArticles.isEmpty())
    }

    @Test
    fun findAllArticles() = testApplicationAsAuthorized { client, _ ->
        val allArticles: List<Article> = articleTestDataGenerator.generateSavedData(NUM_OF_TEST_ARTICLES)

        val response: HttpResponse = client.get(articlesPath)

        val foundArticles: List<Article> = response.body()
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(allArticles.toSet(), foundArticles.toSet())
    }

    @Test
    fun findAllArticlesAsUnauthorized() = testApplicationAsUnauthorized { client ->
        val response: HttpResponse = client.get(articlesPath)

        val errorResponse: ErrorResponse = response.body()
        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertEquals(ErrorResponseType.AUTHENTICATION, errorResponse.errorResponseType)
        assertEquals("Token is not valid or has expired", errorResponse.message)
    }
}