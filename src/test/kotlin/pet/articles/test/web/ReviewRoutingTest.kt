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
import pet.articles.model.dto.payload.RegistrationPayload
import pet.articles.model.dto.payload.ReviewPayload
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
import pet.articles.test.tool.extension.toReviewPayload
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


class ReviewRoutingTest : KoinTest {

    private val reviewsPath = "/api/reviews"

    private val reviewsIdPath = "$reviewsPath/%d"

    private val reviewsUsersPath = "$reviewsPath/users/%d"

    private val reviewsArticlesPath = "$reviewsPath/articles/%d"

    private val dbCleaner: DBCleaner by inject()

    private val reviewService: ReviewService by inject()

    private val reviewTestDataGenerator: TestDataGenerator<Review> by inject(
        named("ReviewTestDataGenerator")
    )

    private val reviewPayloadTestDataGenerator: TestDataGenerator<ReviewPayload> by inject(
        named("ReviewPayloadTestDataGenerator")
    )

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
    fun createReview() = testApplicationAsAuthorized { client, _ ->
        val payload: ReviewPayload = reviewPayloadTestDataGenerator.generateUnsavedData()

        val response: HttpResponse = client.post(reviewsPath) {
            setBody(payload)
        }

        val createdReview: Review = response.body()
        assertEquals(HttpStatusCode.Created, response.status)
        assertTrue(createdReview.isMatches(payload))
    }

    @Test
    fun createReviewAsUnauthorized() = testApplicationAsUnauthorized { client ->
        val payload: ReviewPayload = reviewPayloadTestDataGenerator.generateUnsavedData()

        val response: HttpResponse = client.post(reviewsPath) {
            setBody(payload)
        }

        val errorResponse: ErrorResponse = response.body()
        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertEquals(ErrorResponseType.AUTHENTICATION, errorResponse.errorResponseType)
        assertEquals("Token is not valid or has expired", errorResponse.message)
    }

    @Test
    fun createReviewViaTargetUser() =
        testApplicationAsAuthorized(UserRole.ROLE_USER) { client, authorizedUser ->
        val payload: ReviewPayload = reviewTestDataGenerator
            .generateUnsavedData()
            .toReviewPayload()
            .copy(authorId = authorizedUser.id!!)

        val response: HttpResponse = client.post(reviewsPath) {
            setBody(payload)
        }

        val createdReview: Review = response.body()
        assertEquals(HttpStatusCode.Created, response.status)
        assertTrue(createdReview.isMatches(payload))
    }

    @Test
    fun createReviewWithoutAccess() = testApplicationAsAuthorized(UserRole.ROLE_USER) { client, authorizedUser ->
        val payload: ReviewPayload = reviewPayloadTestDataGenerator.generateUnsavedData()

        val response: HttpResponse = client.post(reviewsPath) {
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
    fun createReviewWithInvalidData() = testApplicationAsAuthorized { client, _ ->
        val invalidPayload: ReviewPayload = reviewPayloadTestDataGenerator.generateInvalidData()

        val response: HttpResponse = client.post(reviewsPath) {
            setBody(invalidPayload)
        }

        val errorResponse: ErrorResponse = response.body()
        assertEquals(HttpStatusCode.UnprocessableEntity, response.status)
        assertEquals(ErrorResponseType.VALIDATION, errorResponse.errorResponseType)
        assertTrue(errorResponse.message.startsWith("Validation failed for ReviewPayload"))
        assertEquals(
            mapOf("content" to "the length of the content must be between 1 and 500"),
            errorResponse.details
        )
    }

    @Test
    fun createReviewWithNonExistentAuthorId() = testApplicationAsAuthorized { client, _ ->
        val unsavedUser: User = userTestDataGenerator.generateUnsavedData()
        val payload: ReviewPayload = reviewTestDataGenerator
            .generateUnsavedData()
            .toReviewPayload()
            .copy(authorId = unsavedUser.id!!)

        val response: HttpResponse = client.post(reviewsPath) {
            setBody(payload)
        }

        val errorResponse: ErrorResponse = response.body()
        assertEquals(HttpStatusCode.InternalServerError, response.status)
        assertTrue(errorResponse.message.startsWith("SQL"))
    }

    @Test
    fun createReviewWithNonExistentArticleId() = testApplicationAsAuthorized { client, _ ->
        val unsavedArticle: Article = articleTestDataGenerator.generateUnsavedData()
        val payload: ReviewPayload = reviewTestDataGenerator
            .generateUnsavedData()
            .toReviewPayload()
            .copy(articleId = unsavedArticle.id!!)

        val response: HttpResponse = client.post(reviewsPath) {
            setBody(payload)
        }

        val errorResponse: ErrorResponse = response.body()
        assertEquals(HttpStatusCode.InternalServerError, response.status)
        assertTrue(errorResponse.message.startsWith("SQL"))
    }

    @Test
    fun deleteReviewById() = testApplicationAsAuthorized { client, _ ->
        val savedReview: Review = reviewTestDataGenerator.generateSavedData()

        val response: HttpResponse = client.delete(reviewsIdPath.format(savedReview.id))

        assertEquals(HttpStatusCode.NoContent, response.status)
    }

    @Test
    fun deleteReviewByIdAsUnauthorized() = testApplicationAsUnauthorized { client ->
        val savedReview: Review = reviewTestDataGenerator.generateSavedData()

        val response: HttpResponse = client.delete(reviewsIdPath.format(savedReview.id))

        val errorResponse: ErrorResponse = response.body()
        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertEquals(ErrorResponseType.AUTHENTICATION, errorResponse.errorResponseType)
        assertEquals("Token is not valid or has expired", errorResponse.message)
    }

    @Test
    fun deleteReviewByIdViaTargetUser() =
        testApplicationAsAuthorized(UserRole.ROLE_USER) { client, authorizedUser ->
        val savedReview: Review = reviewTestDataGenerator
            .generateUnsavedData()
            .copy(authorId = authorizedUser.id)
            .let(reviewService::create)

        val response: HttpResponse = client.delete(reviewsIdPath.format(savedReview.id))

        assertEquals(HttpStatusCode.NoContent, response.status)
    }

    @Test
    fun deleteReviewByIdWithoutAccess() =
        testApplicationAsAuthorized(UserRole.ROLE_USER) { client, authorizedUser ->
        val savedReview: Review = reviewTestDataGenerator.generateSavedData()

         val path = reviewsIdPath.format(savedReview.id)
        val response: HttpResponse = client.delete(path)

        val errorResponse: ErrorResponse = response.body()
        assertEquals(HttpStatusCode.Forbidden, response.status)
        assertEquals(ErrorResponseType.AUTHENTICATION, errorResponse.errorResponseType)
        assertEquals(
            "User with username = ${authorizedUser.username} doesn't have the required permissions",
            errorResponse.message
        )
    }

    @Test
    fun deleteReviewByNonExistentId() = testApplicationAsAuthorized { client, _ ->
        val unsavedReview: Review = reviewTestDataGenerator.generateUnsavedData()

        val response: HttpResponse = client.delete(reviewsIdPath.format(unsavedReview.id))

        assertEquals(HttpStatusCode.NoContent, response.status)
    }

    @Test
    fun findReviewById() = testApplicationAsAuthorized { client, _ ->
        val savedReview: Review = reviewTestDataGenerator.generateSavedData()

        val response: HttpResponse = client.get(reviewsIdPath.format(savedReview.id))

        val foundReview: Review = response.body()
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(savedReview, foundReview)
    }

    @Test
    fun findReviewByIdAsUnauthorized() = testApplicationAsUnauthorized { client ->
        val savedReview: Review = reviewTestDataGenerator.generateSavedData()

        val response: HttpResponse = client.get(reviewsIdPath.format(savedReview.id))

        val errorResponse: ErrorResponse = response.body()
        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertEquals(ErrorResponseType.AUTHENTICATION, errorResponse.errorResponseType)
        assertEquals("Token is not valid or has expired", errorResponse.message)
    }

    @Test
    fun findReviewByNonExistentId() = testApplicationAsAuthorized { client, _ ->
        val unsavedReview: Review = reviewTestDataGenerator.generateUnsavedData()

        val response: HttpResponse = client.get(reviewsIdPath.format(unsavedReview.id))

        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun findReviewsByAuthorId() = testApplicationAsAuthorized { client, _ ->
        val savedReviews: List<Review> = reviewTestDataGenerator.generateSavedData(100)
        val authorId: Int = savedReviews.random().authorId!!
        val expectedReviews = savedReviews.filter { it.authorId == authorId }

        val response: HttpResponse = client.get(reviewsUsersPath.format(authorId))

        val foundReviews: List<Review> = response.body()
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(expectedReviews.toSet(), foundReviews.toSet())
    }

    @Test
    fun findReviewsByAuthorIdAsUnauthorized() = testApplicationAsUnauthorized { client ->
        val savedReviews: List<Review> = reviewTestDataGenerator.generateSavedData(100)
        val authorId: Int = savedReviews.random().authorId!!

        val response: HttpResponse = client.get(reviewsUsersPath.format(authorId))

        val errorResponse: ErrorResponse = response.body()
        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertEquals(ErrorResponseType.AUTHENTICATION, errorResponse.errorResponseType)
        assertEquals("Token is not valid or has expired", errorResponse.message)
    }

    @Test
    fun findReviewsByNonExistentAuthorId() = testApplicationAsAuthorized { client, _ ->
        val unsavedUser: User = userTestDataGenerator.generateUnsavedData()

        val response: HttpResponse = client.get(reviewsUsersPath.format(unsavedUser.id))

        val foundReviews: List<Review> = response.body()
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(foundReviews.isEmpty())
    }

    @Test
    fun findReviewsByArticleId() = testApplicationAsAuthorized { client, _ ->
        val savedReviews: List<Review> = reviewTestDataGenerator.generateSavedData(100)
        val articleId: Int = savedReviews.first().articleId
        val expectedReviews = savedReviews.filter { it.articleId == articleId }

        val response: HttpResponse = client.get(reviewsArticlesPath.format(articleId))

        val foundReviews: List<Review> = response.body()
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(expectedReviews.toSet(), foundReviews.toSet())
    }

    @Test
    fun findReviewsByArticleIdAsUnauthorized() = testApplicationAsUnauthorized { client ->
        val savedReviews: List<Review> = reviewTestDataGenerator.generateSavedData(100)
        val articleId: Int = savedReviews.first().articleId

        val response: HttpResponse = client.get(reviewsArticlesPath.format(articleId))

        val errorResponse: ErrorResponse = response.body()
        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertEquals(ErrorResponseType.AUTHENTICATION, errorResponse.errorResponseType)
        assertEquals("Token is not valid or has expired", errorResponse.message)
    }


    @Test
    fun findReviewsByNonExistentArticleId() = testApplicationAsAuthorized { client, _ ->
        val unsavedArticle: Article = articleTestDataGenerator.generateUnsavedData()

        val response: HttpResponse = client.get(reviewsArticlesPath.format(unsavedArticle.id))

        val foundReviews: List<Review> = response.body()
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(foundReviews.isEmpty())
    }
}