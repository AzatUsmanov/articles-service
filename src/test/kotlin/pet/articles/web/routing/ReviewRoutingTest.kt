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
import pet.articles.model.dto.Review
import pet.articles.model.dto.User
import pet.articles.model.dto.payload.ReviewPayload
import pet.articles.model.enums.ErrorResponseType
import pet.articles.model.enums.UserRole
import pet.articles.service.review.ReviewService
import pet.articles.tool.testing.extension.KoinConfigureTestExtension
import pet.articles.tool.extension.getProperty
import pet.articles.tool.extension.isMatches
import pet.articles.tool.extension.toReviewPayload
import pet.articles.tool.generator.TestDataGenerator
import pet.articles.tool.testing.extension.DBCleanupExtension
import pet.articles.web.testApplicationAsAuthorized
import pet.articles.web.testApplicationAsUnauthorized
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


@ExtendWith(DBCleanupExtension::class)
@ExtendWith(KoinConfigureTestExtension::class)
class ReviewRoutingTest : KoinTest {

    companion object {
        const val NUM_OF_TEST_REVIEWS = 10
    }

    private val reviewsPath = getProperty("api.paths.reviews")

    private val reviewsIdPath = "$reviewsPath/%d"

    private val reviewsUsersPath = "${getProperty("api.paths.reviews.users")}/%d"

    private val reviewsArticlesPath = "${getProperty("api.paths.reviews.articles")}/%d"
    
    private val reviewService: ReviewService by inject()

    private val reviewGenerator: TestDataGenerator<Review> by inject(
        named("ReviewTestDataGenerator")
    )

    private val reviewPayloadGenerator: TestDataGenerator<ReviewPayload> by inject(
        named("ReviewPayloadTestDataGenerator")
    )

    private val userGenerator: TestDataGenerator<User> by inject(
        named("UserTestDataGenerator")
    )

    private val articleGenerator: TestDataGenerator<Article> by inject(
        named("ArticleTestDataGenerator")
    )

    @Test
    fun createReview() = testApplicationAsAuthorized { client, _ ->
        val payload: ReviewPayload = reviewPayloadGenerator.generateUnsavedData()

        val response: HttpResponse = client.post(reviewsPath) {
            setBody(payload)
        }

        val createdReview: Review = response.body()
        assertEquals(HttpStatusCode.Created, response.status)
        assertTrue(createdReview.isMatches(payload))
    }

    @Test
    fun createReviewAsUnauthorized() = testApplicationAsUnauthorized { client ->
        val payload: ReviewPayload = reviewPayloadGenerator.generateUnsavedData()

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
        val payload: ReviewPayload = reviewGenerator
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
        val payload: ReviewPayload = reviewPayloadGenerator.generateUnsavedData()

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
        val invalidPayload: ReviewPayload = reviewPayloadGenerator.generateInvalidData()

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
        val unsavedUser: User = userGenerator.generateUnsavedData()
        val payload: ReviewPayload = reviewGenerator
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
        val unsavedArticle: Article = articleGenerator.generateUnsavedData()
        val payload: ReviewPayload = reviewGenerator
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
        val savedReview: Review = reviewGenerator.generateSavedData()

        val response: HttpResponse = client.delete(reviewsIdPath.format(savedReview.id))

        assertEquals(HttpStatusCode.NoContent, response.status)
    }

    @Test
    fun deleteReviewByIdAsUnauthorized() = testApplicationAsUnauthorized { client ->
        val savedReview: Review = reviewGenerator.generateSavedData()

        val response: HttpResponse = client.delete(reviewsIdPath.format(savedReview.id))

        val errorResponse: ErrorResponse = response.body()
        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertEquals(ErrorResponseType.AUTHENTICATION, errorResponse.errorResponseType)
        assertEquals("Token is not valid or has expired", errorResponse.message)
    }

    @Test
    fun deleteReviewByIdViaTargetUser() =
        testApplicationAsAuthorized(UserRole.ROLE_USER) { client, authorizedUser ->
        val savedReview: Review = reviewGenerator
            .generateUnsavedData()
            .copy(authorId = authorizedUser.id)
            .let(reviewService::create)

        val response: HttpResponse = client.delete(reviewsIdPath.format(savedReview.id))

        assertEquals(HttpStatusCode.NoContent, response.status)
    }

    @Test
    fun deleteReviewByIdWithoutAccess() =
        testApplicationAsAuthorized(UserRole.ROLE_USER) { client, authorizedUser ->
        val savedReview: Review = reviewGenerator.generateSavedData()

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
        val unsavedReview: Review = reviewGenerator.generateUnsavedData()

        val response: HttpResponse = client.delete(reviewsIdPath.format(unsavedReview.id))

        assertEquals(HttpStatusCode.NoContent, response.status)
    }

    @Test
    fun findReviewById() = testApplicationAsAuthorized { client, _ ->
        val savedReview: Review = reviewGenerator.generateSavedData()

        val response: HttpResponse = client.get(reviewsIdPath.format(savedReview.id))

        val foundReview: Review = response.body()
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(savedReview, foundReview)
    }

    @Test
    fun findReviewByIdAsUnauthorized() = testApplicationAsUnauthorized { client ->
        val savedReview: Review = reviewGenerator.generateSavedData()

        val response: HttpResponse = client.get(reviewsIdPath.format(savedReview.id))

        val errorResponse: ErrorResponse = response.body()
        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertEquals(ErrorResponseType.AUTHENTICATION, errorResponse.errorResponseType)
        assertEquals("Token is not valid or has expired", errorResponse.message)
    }

    @Test
    fun findReviewByNonExistentId() = testApplicationAsAuthorized { client, _ ->
        val unsavedReview: Review = reviewGenerator.generateUnsavedData()

        val response: HttpResponse = client.get(reviewsIdPath.format(unsavedReview.id))

        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun findReviewsByAuthorId() = testApplicationAsAuthorized { client, _ ->
        val savedReviews: List<Review> = reviewGenerator.generateSavedData(NUM_OF_TEST_REVIEWS)
        val authorId: Int = savedReviews.random().authorId!!
        val expectedReviews = savedReviews.filter { it.authorId == authorId }

        val response: HttpResponse = client.get(reviewsUsersPath.format(authorId))

        val foundReviews: List<Review> = response.body()
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(expectedReviews.toSet(), foundReviews.toSet())
    }

    @Test
    fun findReviewsByAuthorIdAsUnauthorized() = testApplicationAsUnauthorized { client ->
        val savedReviews: List<Review> = reviewGenerator.generateSavedData(NUM_OF_TEST_REVIEWS)
        val authorId: Int = savedReviews.random().authorId!!

        val response: HttpResponse = client.get(reviewsUsersPath.format(authorId))

        val errorResponse: ErrorResponse = response.body()
        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertEquals(ErrorResponseType.AUTHENTICATION, errorResponse.errorResponseType)
        assertEquals("Token is not valid or has expired", errorResponse.message)
    }

    @Test
    fun findReviewsByNonExistentAuthorId() = testApplicationAsAuthorized { client, _ ->
        val unsavedUser: User = userGenerator.generateUnsavedData()

        val response: HttpResponse = client.get(reviewsUsersPath.format(unsavedUser.id))

        val foundReviews: List<Review> = response.body()
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(foundReviews.isEmpty())
    }

    @Test
    fun findReviewsByArticleId() = testApplicationAsAuthorized { client, _ ->
        val savedReviews: List<Review> = reviewGenerator.generateSavedData(NUM_OF_TEST_REVIEWS)
        val articleId: Int = savedReviews.first().articleId
        val expectedReviews = savedReviews.filter { it.articleId == articleId }

        val response: HttpResponse = client.get(reviewsArticlesPath.format(articleId))

        val foundReviews: List<Review> = response.body()
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(expectedReviews.toSet(), foundReviews.toSet())
    }

    @Test
    fun findReviewsByArticleIdAsUnauthorized() = testApplicationAsUnauthorized { client ->
        val savedReviews: List<Review> = reviewGenerator.generateSavedData(NUM_OF_TEST_REVIEWS)
        val articleId: Int = savedReviews.first().articleId

        val response: HttpResponse = client.get(reviewsArticlesPath.format(articleId))

        val errorResponse: ErrorResponse = response.body()
        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertEquals(ErrorResponseType.AUTHENTICATION, errorResponse.errorResponseType)
        assertEquals("Token is not valid or has expired", errorResponse.message)
    }


    @Test
    fun findReviewsByNonExistentArticleId() = testApplicationAsAuthorized { client, _ ->
        val unsavedArticle: Article = articleGenerator.generateUnsavedData()

        val response: HttpResponse = client.get(reviewsArticlesPath.format(unsavedArticle.id))

        val foundReviews: List<Review> = response.body()
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(foundReviews.isEmpty())
    }
}