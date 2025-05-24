package pet.articles.tool.generator

import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

import pet.articles.model.dto.Article
import pet.articles.model.dto.User
import pet.articles.model.dto.payload.NewArticlePayload
import pet.articles.tool.generator.ArticleTestDataGenerator.Companion.generateSavedAuthorIds

class NewArticlePayloadTestDataGenerator(
    private val articleGenerator: TestDataGenerator<Article>,
    private val userGenerator: TestDataGenerator<User>
) : TestDataGenerator<NewArticlePayload> {

    override fun generateSavedData(dataSize: Int): List<NewArticlePayload> =
        generateData(dataSize, articleGenerator::generateSavedData)

    override fun generateUnsavedData(dataSize: Int): List<NewArticlePayload> =
        generateData(dataSize, articleGenerator::generateUnsavedData)

    override fun generateInvalidData(): NewArticlePayload =
        convertToNewArticlePayload(
            articleGenerator.generateInvalidData(),
            generateSavedAuthorIds(userGenerator)
        )

    private fun convertToNewArticlePayload(
        article: Article,
        savedAuthorIds: List<Int>
    ): NewArticlePayload = NewArticlePayload(
        topic = article.topic,
        content = article.content,
        authorIds = savedAuthorIds
    )

    private fun generateData(dataSize: Int, generate: (Int) -> List<Article>): List<NewArticlePayload> {
        val savedAuthorIds: List<Int> = generateSavedAuthorIds(userGenerator)
        return generate(dataSize).map { article ->
            convertToNewArticlePayload(article, savedAuthorIds)
        }
    }
}