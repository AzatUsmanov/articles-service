package pet.articles.tool.generator

import net.datafaker.Faker

import pet.articles.model.dto.Article
import pet.articles.model.dto.Review
import pet.articles.model.dto.User
import pet.articles.model.enums.ReviewType
import pet.articles.service.review.ReviewService
import pet.articles.tool.extension.generateRandom
import pet.articles.web.validation.ReviewValidation.Fields.Constraints.CONTENT_MAX_LENGTH

import java.time.LocalDateTime

class ReviewTestDataGenerator(
    private val reviewService: ReviewService,
    private val userGenerator: TestDataGenerator<User>,
    private val articleGenerator: TestDataGenerator<Article>,
    private val faker: Faker = Faker()
) : TestDataGenerator<Review> {

    companion object {
        const val REVIEW_FIELD_TOPIC_INVALID_LENGTH= 1000

        const val NUM_OF_TEST_USERS = 10
        const val NUM_OF_TEST_REVIEWS = 10

        const val CONTENT_PARAGRAPH_SIZE = 1
    }

    override fun generateUnsavedData(dataSize: Int): List<Review> {
        val savedUserIds: List<Int> = userGenerator.generateSavedData(NUM_OF_TEST_USERS)
            .map(User::id)
            .map{ it!! }
        val savedArticleIds: List<Int> = articleGenerator.generateSavedData(NUM_OF_TEST_REVIEWS)
            .map(Article::id)
            .map{ it!! }

        return (1..dataSize).map {
            Review(
                id = faker.number().positive(),
                type = ReviewType.entries.random(),
                dateOfCreation = LocalDateTime.now(),
                content = faker.lorem().paragraph(CONTENT_PARAGRAPH_SIZE).take(CONTENT_MAX_LENGTH),
                authorId = savedUserIds.random(),
                articleId = savedArticleIds.random()
            )
        }
    }

    override fun generateSavedData(dataSize: Int): List<Review> =
        generateUnsavedData(dataSize).map(reviewService::create)

    override fun generateInvalidData(): Review = generateUnsavedData().copy(
        content = String.generateRandom(REVIEW_FIELD_TOPIC_INVALID_LENGTH)
    )
}