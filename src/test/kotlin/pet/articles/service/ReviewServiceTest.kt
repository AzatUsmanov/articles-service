package pet.articles.service

import org.junit.jupiter.api.Assertions.assertEquals
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
import pet.articles.model.dto.Review
import pet.articles.model.dto.User
import pet.articles.service.article.ArticleService
import pet.articles.service.review.ReviewService
import pet.articles.service.user.UserService
import pet.articles.tool.generator.TestDataGenerator
import pet.articles.tool.testing.extension.DBCleanupExtension
import pet.articles.tool.testing.extension.KoinConfigureTestExtension

@ExtendWith(DBCleanupExtension::class)
@ExtendWith(KoinConfigureTestExtension::class)
class ReviewServiceTest : KoinTest {

    companion object {
        const val NUM_OF_TEST_REVIEWS = 10
    }

    private val reviewService: ReviewService by inject()

    private val articleService: ArticleService by inject()

    private val userService: UserService by inject()

    private val userGenerator: TestDataGenerator<User> by inject(
        named("UserTestDataGenerator")
    )

    private val articleGenerator: TestDataGenerator<Article> by inject(
        named("ArticleTestDataGenerator")
    )

    private val reviewGenerator: TestDataGenerator<Review> by inject(
        named("ReviewTestDataGenerator")
    )

    @Test
    fun createReview() {
        val reviewForSave: Review = reviewGenerator.generateUnsavedData()

        val savedReview: Review = reviewService.create(reviewForSave)

        val reviewForCheck: Review? = reviewService.findById(savedReview.id!!)
        assertNotNull(reviewForCheck)
        assertEquals(savedReview, reviewForCheck)
    }

    @Test
    fun createReviewWithNonExistentArticleId() {
        val unsavedArticle: Article = articleGenerator.generateUnsavedData()
        val reviewForSave: Review = reviewGenerator.generateUnsavedData().copy(
            articleId = unsavedArticle.id!!
        )

        assertThrows(RuntimeException::class.java) {
            reviewService.create(reviewForSave)
        }
    }

    @Test
    fun createReviewWithNonExistentAuthorId() {
        val unsavedAuthor: User = userGenerator.generateUnsavedData()
        val reviewForSave: Review = reviewGenerator.generateUnsavedData().copy(
            authorId = unsavedAuthor.id!!
        )

        assertThrows(RuntimeException::class.java) {
            reviewService.create(reviewForSave)
        }
    }

    @Test
    fun deleteReviewById() {
        val savedReview: Review = reviewGenerator.generateSavedData()

        reviewService.deleteById(savedReview.id!!)

        val reviewForCheck: Review? = reviewService.findById(savedReview.id!!)
        assertNull(reviewForCheck)
    }

    @Test
    fun deleteReviewByNonExistentId() {
        val unsavedReview: Review = reviewGenerator.generateUnsavedData()

        reviewService.deleteById(unsavedReview.id!!)

        val reviewForCheck: Review? = reviewService.findById(unsavedReview.id!!)
        assertNull(reviewForCheck)
    }

    @Test
    fun deleteReviewWhenDeletingArticle() {
        val savedReview: Review = reviewGenerator.generateSavedData()
        val article: Article = articleService.findById(savedReview.articleId)!!

        articleService.deleteById(article.id!!)

        val reviewForCheck: Review? = reviewService.findById(savedReview.id!!)
        assertNull(reviewForCheck)
    }

    @Test
    fun deleteReviewWhenDeletingAuthor() {
        val savedReview: Review = reviewGenerator.generateSavedData()
        val author: User = userService.findById(savedReview.authorId!!)!!

        userService.deleteById(author.id!!)

        val reviewForCheck: Review? = reviewService.findById(savedReview.id!!)
        assertNotNull(reviewForCheck)
        assertNull(reviewForCheck!!.authorId)
    }

    @Test
    fun findReviewById() {
        val savedReview: Review = reviewGenerator.generateSavedData()

        val reviewForCheck: Review? = reviewService.findById(savedReview.id!!)

        assertNotNull(reviewForCheck)
        assertEquals(savedReview, reviewForCheck)
    }

    @Test
    fun findReviewByNonExistentId() {
        val unsavedReview: Review = reviewGenerator.generateUnsavedData()

        val reviewForCheck: Review? = reviewService.findById(unsavedReview.id!!)

        assertNull(reviewForCheck)
    }

    @Test
    fun findReviewsByAuthorId() {
        val reviews: List<Review> = reviewGenerator.generateSavedData(NUM_OF_TEST_REVIEWS)
        val authorId: Int = reviews.random().authorId!!
        val allReviewsByAuthor: List<Review> = reviews.filter { it.authorId == authorId }

        val reviewsForCheck: List<Review> = reviewService.findByAuthorId(authorId)

        assertEquals(allReviewsByAuthor.size, reviewsForCheck.size)
        assertTrue(allReviewsByAuthor.containsAll(reviewsForCheck))
        assertTrue(reviewsForCheck.containsAll(allReviewsByAuthor))
    }

    @Test
    fun findReviewsByNonExistentAuthorId() {
        val unsavedAuthor: User = userGenerator.generateUnsavedData()

        val reviews: List<Review> = reviewService.findByAuthorId(unsavedAuthor.id!!)

        assertTrue(reviews.isEmpty())
    }

    @Test
    fun findReviewsByArticleId() {
        val reviews: List<Review> = reviewGenerator.generateSavedData(NUM_OF_TEST_REVIEWS)
        val articleId: Int = reviews.random().articleId
        val allReviewsForArticle: List<Review> = reviews.filter { it.articleId == articleId }

        val reviewsForCheck: List<Review> = reviewService.findByArticleId(articleId)

        assertEquals(allReviewsForArticle.size, reviewsForCheck.size)
        assertTrue(allReviewsForArticle.containsAll(reviewsForCheck))
        assertTrue(reviewsForCheck.containsAll(allReviewsForArticle))
    }

    @Test
    fun findReviewsByNonExistentArticleId() {
        val unsavedArticle: Article = articleGenerator.generateUnsavedData()

        val reviews: List<Review> = reviewService.findByArticleId(unsavedArticle.id!!)

        assertTrue(reviews.isEmpty())
    }
}