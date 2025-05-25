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
    private val articleGenerator: TestDataGenerator<Article>
) : TestDataGeneratorBaseImpl<Review>(
    generate = { generate(userGenerator, articleGenerator) },
    create = reviewService::create,
    toInvalidState = ::makeInvalid
) {

    companion object {
        private const val INVALID_CONTENT_LENGTH = 1000
        private const val CONTENT_PARAGRAPH_SIZE = 1
        private const val NUM_OF_TEST_USERS = 3
        private const val NUM_OF_TEST_ARTICLES = 3

        private fun generate(
            userGenerator: TestDataGenerator<User>,
            articleGenerator: TestDataGenerator<Article>,
            faker: Faker = Faker()
        ): Review {
            val savedUserIds: List<Int> = generateSavedUserIds(userGenerator)
            val savedArticleIds: List<Int> = generateSavedArticleIds(articleGenerator)
            return Review(
                id = faker.number().positive(),
                type = ReviewType.entries.random(),
                dateOfCreation = LocalDateTime.now(),
                authorId = savedUserIds.random(),
                articleId = savedArticleIds.random(),
                content = faker
                    .lorem()
                    .paragraph(CONTENT_PARAGRAPH_SIZE)
                    .take(CONTENT_MAX_LENGTH)
            )
        }

        private fun makeInvalid(review: Review) = review.copy(
            content = String.generateRandom(INVALID_CONTENT_LENGTH)
        )

        private fun generateSavedUserIds(
            userGenerator: TestDataGenerator<User>
        ): List<Int> = userGenerator
            .generateSavedData(NUM_OF_TEST_USERS)
            .mapNotNull(User::id)

        private fun generateSavedArticleIds(
            articleGenerator: TestDataGenerator<Article>
        ): List<Int> = articleGenerator
            .generateSavedData(NUM_OF_TEST_ARTICLES)
            .mapNotNull(Article::id)
    }
}