package pet.articles.tool.generator

import net.datafaker.Faker

import pet.articles.model.dto.User
import pet.articles.service.article.ArticleService
import pet.articles.model.dto.Article
import pet.articles.tool.extension.generateRandom
import pet.articles.web.validation.ArticlePayloadValidation.Fields.Constraints.CONTENT_MAX_LENGTH
import pet.articles.web.validation.ArticlePayloadValidation.Fields.Constraints.TOPIC_MAX_LENGTH

import java.time.LocalDateTime

class ArticleTestDataGenerator(
    private val articleService: ArticleService,
    private val userGenerator: TestDataGenerator<User>
) : TestDataGeneratorBaseImpl<Article>(
    generate = ::generate,
    create = { article ->
        articleService.create(article, generateSavedAuthorIds(userGenerator))
    },
    toInvalidState = ::makeInvalid
) {

    companion object {
        private const val ARTICLE_FIELD_TOPIC_INVALID_LENGTH = 1000
        private const val CONTENT_PARAGRAPH_SIZE = 3
        private const val CONTENT_SENTENCE_SIZE = 3
        private const val MIN_NUM_OF_TEST_AUTHOR_IDS = 1
        private const val MAX_NUM_OF_TEST_AUTHOR_IDS = 5

        private fun generate(faker: Faker = Faker()) = Article(
            id = faker.number().positive(),
            dateOfCreation = LocalDateTime.now(),
            topic = faker
                .lorem()
                .sentence(CONTENT_SENTENCE_SIZE)
                .take(TOPIC_MAX_LENGTH),
            content = faker
                .lorem()
                .paragraph(CONTENT_PARAGRAPH_SIZE)
                .take(CONTENT_MAX_LENGTH)
        )

        private fun makeInvalid(article: Article) = article.copy(
            topic = String.generateRandom(ARTICLE_FIELD_TOPIC_INVALID_LENGTH)
        )

        fun generateSavedAuthorIds(
            userGenerator: TestDataGenerator<User>
        ): List<Int> {
            val numOfAuthors: Int = (MIN_NUM_OF_TEST_AUTHOR_IDS..MAX_NUM_OF_TEST_AUTHOR_IDS)
                .random()
            return userGenerator.generateSavedData(numOfAuthors)
                .map { it.id!! }
        }
    }
}