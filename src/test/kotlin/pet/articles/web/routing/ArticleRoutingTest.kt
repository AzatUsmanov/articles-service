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
import pet.articles.model.dto.payload.NewArticlePayload
import pet.articles.model.dto.payload.UpdateArticlePayload
import pet.articles.model.enums.ErrorResponseType
import pet.articles.model.enums.UserRole
import pet.articles.service.article.ArticleService
import pet.articles.tool.testing.extension.KoinConfigureTestExtension
import pet.articles.tool.extension.getProperty
import pet.articles.tool.extension.isMatches
import pet.articles.tool.extension.toUpdateArticlePayload
import pet.articles.tool.generator.TestDataGenerator
import pet.articles.tool.testing.extension.DBCleanupExtension
import pet.articles.web.testApplicationAsAuthorized
import pet.articles.web.testApplicationAsUnauthorized
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


@ExtendWith(DBCleanupExtension::class)
@ExtendWith(KoinConfigureTestExtension::class)
class ArticleRoutingTest : KoinTest {

    companion object {
        const val NUM_OF_TEST_ARTICLES = 10
    }

    private val articlesPath = getProperty("api.paths.articles")

    private val articlesIdPath = "$articlesPath/%d"

    private val articlesAuthorshipPath = "${getProperty("api.paths.articles.authorship")}/%d"
    
    private val articleService: ArticleService by inject()

    private val articleGenerator: TestDataGenerator<Article> by inject(
        named("ArticleTestDataGenerator")
    )

    private val newArticlePayloadGenerator: TestDataGenerator<NewArticlePayload> by inject(
        named("NewArticlePayloadTestDataGenerator")
    )

    private val updateArticlePayloadGenerator: TestDataGenerator<UpdateArticlePayload> by inject(
        named("UpdateArticlePayloadTestDataGenerator")
    )

    private val userGenerator: TestDataGenerator<User> by inject(
        named("UserTestDataGenerator")
    )

    @Test
    fun createArticle() = testApplicationAsAuthorized { client, _ ->
        val payload: NewArticlePayload = newArticlePayloadGenerator.generateUnsavedData()

        val response: HttpResponse = client.post(articlesPath) {
            setBody(payload)
        }

        val createdArticle: Article = response.body()
        assertEquals(HttpStatusCode.Created, response.status)
        assertTrue(createdArticle.isMatches(payload))
    }

    @Test
    fun createArticleAsUnauthorized() = testApplicationAsUnauthorized { client ->
        val payload: NewArticlePayload = newArticlePayloadGenerator.generateUnsavedData()

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
            val payload: NewArticlePayload = newArticlePayloadGenerator
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
            val payload: NewArticlePayload = newArticlePayloadGenerator.generateUnsavedData()

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
        val invalidPayload: NewArticlePayload = newArticlePayloadGenerator.generateInvalidData()

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
        val savedArticle: Article = articleGenerator.generateSavedData()
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
        val savedArticle: Article = articleGenerator.generateSavedData()
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
            val savedArticle: Article = articleGenerator
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
            val savedArticle: Article = articleGenerator.generateSavedData()
            val payload: UpdateArticlePayload = updateArticlePayloadGenerator.generateUnsavedData()

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
        val unsavedArticle: Article = articleGenerator.generateUnsavedData()
        val payload: UpdateArticlePayload = updateArticlePayloadGenerator.generateUnsavedData()

        val response: HttpResponse = client.patch(articlesIdPath.format(unsavedArticle.id!!)) {
            setBody(payload)
        }

        val errorResponse: ErrorResponse = response.body()
        assertEquals(HttpStatusCode.InternalServerError, response.status)
        assertEquals(ErrorResponseType.COMMON, errorResponse.errorResponseType)
        assertTrue(errorResponse.message.startsWith("Attempt to update"))
    }

    @Test
    fun updateArticleByIdWithInvalidData() = testApplicationAsAuthorized { client, _ ->
        val savedArticle: Article = articleGenerator.generateSavedData()
        val invalidPayload: UpdateArticlePayload = updateArticlePayloadGenerator.generateInvalidData()

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
        val savedArticle: Article = articleGenerator.generateSavedData()

        val response: HttpResponse = client.delete(articlesIdPath.format(savedArticle.id))

        assertEquals(HttpStatusCode.NoContent, response.status)
    }

    @Test
    fun deleteArticleByIdAsUnauthorized() = testApplicationAsUnauthorized { client ->
        val savedArticle: Article = articleGenerator.generateSavedData()

        val response: HttpResponse = client.delete(articlesIdPath.format(savedArticle.id))

        val errorResponse: ErrorResponse = response.body()
        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertEquals(ErrorResponseType.AUTHENTICATION, errorResponse.errorResponseType)
        assertEquals("Token is not valid or has expired", errorResponse.message)
    }

    @Test
    fun deleteArticleByIdViaTargetUser() =
        testApplicationAsAuthorized(UserRole.ROLE_USER) { client, authorizedUser ->
            val savedArticle: Article = articleGenerator
                .generateUnsavedData()
                .let { article -> articleService.create(article, listOf(authorizedUser.id!!)) }

            val response: HttpResponse = client.delete(articlesIdPath.format(savedArticle.id))

            assertEquals(HttpStatusCode.NoContent, response.status)
        }

    @Test
    fun deleteArticleByIdWithoutAccess() =
        testApplicationAsAuthorized(UserRole.ROLE_USER) { client, authorizedUser ->
            val savedArticle: Article = articleGenerator.generateSavedData()

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
        val unsavedArticle: Article = articleGenerator.generateUnsavedData()

        val response: HttpResponse = client.delete(articlesIdPath.format(unsavedArticle.id))

        assertEquals(HttpStatusCode.NoContent, response.status)
    }

    @Test
    fun findArticleById() = testApplicationAsAuthorized { client, _ ->
        val savedArticle: Article = articleGenerator.generateSavedData()

        val response: HttpResponse = client.get(articlesIdPath.format(savedArticle.id))

        val foundArticle: Article = response.body()
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(savedArticle, foundArticle)
    }

    @Test
    fun findArticleByIdAsUnauthorized() = testApplicationAsUnauthorized { client ->
        val savedArticle: Article = articleGenerator.generateSavedData()

        val response: HttpResponse = client.get(articlesIdPath.format(savedArticle.id))

        val errorResponse: ErrorResponse = response.body()
        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertEquals(ErrorResponseType.AUTHENTICATION, errorResponse.errorResponseType)
        assertEquals("Token is not valid or has expired", errorResponse.message)
    }

    @Test
    fun findArticleByNonExistentId() = testApplicationAsAuthorized { client, _ ->
        val unsavedArticle: Article = articleGenerator.generateUnsavedData()

        val response: HttpResponse = client.get(articlesIdPath.format(unsavedArticle.id))

        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun findArticlesByAuthorId() = testApplicationAsAuthorized { client, _ ->
        val savedAuthor: User = userGenerator.generateSavedData()
        val savedArticles: List<Article> = articleGenerator
            .generateUnsavedData(NUM_OF_TEST_ARTICLES)
            .map { articleService.create(it, listOf(savedAuthor.id!!)) }

        val response: HttpResponse = client.get(articlesAuthorshipPath.format(savedAuthor.id))

        val foundArticles: List<Article> = response.body()
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(savedArticles.toSet(), foundArticles.toSet())
    }

    @Test
    fun indArticlesByAuthorIdAsUnauthorized() = testApplicationAsUnauthorized { client ->
        val savedAuthor: User = userGenerator.generateSavedData()
        val savedArticles: List<Article> = articleGenerator
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
        val unsavedAuthor: User = userGenerator.generateUnsavedData()

        val response: HttpResponse = client.get(articlesAuthorshipPath.format(unsavedAuthor.id))

        val foundArticles: List<Article> = response.body()
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(foundArticles.isEmpty())
    }

    @Test
    fun findAllArticles() = testApplicationAsAuthorized { client, _ ->
        val allArticles: List<Article> = articleGenerator.generateSavedData(NUM_OF_TEST_ARTICLES)

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