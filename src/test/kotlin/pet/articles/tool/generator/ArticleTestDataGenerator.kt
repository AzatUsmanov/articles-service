package pet.articles.tool.generator

import net.datafaker.Faker
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

import pet.articles.model.dto.User
import pet.articles.service.ArticleService
import pet.articles.model.dto.Article
import pet.articles.tool.extension.generateRandom
import pet.articles.web.validation.ArticlePayloadValidation.Fields.Constraints.CONTENT_MAX_LENGTH
import pet.articles.web.validation.ArticlePayloadValidation.Fields.Constraints.TOPIC_MAX_LENGTH

import java.time.LocalDateTime

class ArticleTestDataGenerator(
    private val articleService: ArticleService,
    private val userGenerator: TestDataGenerator<User>,
    private val faker: Faker = Faker()
) : TestDataGenerator<Article> {

    companion object {
        const val ARTICLE_FIELD_TOPIC_INVALID_LENGTH = 1000

        const val CONTENT_PARAGRAPH_SIZE = 3
        const val CONTENT_SENTENCE_SIZE = 3

        private const val MIN_NUM_OF_TEST_AUTHOR_IDS = 1
        private const val MAX_NUM_OF_TEST_AUTHOR_IDS = 5

        fun generateSavedAuthorIds(userGenerator: TestDataGenerator<User>): List<Int> =
            userGenerator.generateSavedData(generateRandomSizeOfAuthorIdsList())
                .map{ user -> user.id!! }
                .toMutableList()

        private fun generateRandomSizeOfAuthorIdsList() =
            (MIN_NUM_OF_TEST_AUTHOR_IDS..MAX_NUM_OF_TEST_AUTHOR_IDS).random()
    }

    override fun generateUnsavedData(dataSize: Int): List<Article> =
        (1..dataSize).map {
            Article(
                id = faker.number().positive(),
                dateOfCreation = LocalDateTime.now(),
                topic = faker.lorem().sentence(CONTENT_SENTENCE_SIZE).take(TOPIC_MAX_LENGTH),
                content = faker.lorem().paragraph(CONTENT_PARAGRAPH_SIZE).take(CONTENT_MAX_LENGTH)
            )
        }

    override fun generateSavedData(dataSize: Int): List<Article> =
        generateUnsavedData(dataSize).map { article ->
            articleService.create(article, generateSavedAuthorIds(userGenerator))
        }

    override fun generateInvalidData(): Article = generateUnsavedData().copy(
        topic = String.generateRandom(ARTICLE_FIELD_TOPIC_INVALID_LENGTH)
    )
}

