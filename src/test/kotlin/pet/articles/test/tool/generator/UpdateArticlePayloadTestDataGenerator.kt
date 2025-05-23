package pet.articles.test.tool.generator

import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

import pet.articles.model.dto.Article
import pet.articles.model.dto.payload.UpdateArticlePayload

class UpdateArticlePayloadTestDataGenerator(
    private val articleTestDataGenerator: TestDataGenerator<Article>
) : TestDataGenerator<UpdateArticlePayload> {

    override fun generateSavedData(dataSize: Int): List<UpdateArticlePayload> =
        articleTestDataGenerator.generateSavedData(dataSize).map(::convertToUpdateArticlePayload)

    override fun generateUnsavedData(dataSize: Int): List<UpdateArticlePayload> =
        articleTestDataGenerator.generateUnsavedData(dataSize).map(::convertToUpdateArticlePayload)

    override fun generateInvalidData(): UpdateArticlePayload =
        convertToUpdateArticlePayload(articleTestDataGenerator.generateInvalidData())

    private fun convertToUpdateArticlePayload(article: Article): UpdateArticlePayload =
        UpdateArticlePayload(
            topic = article.topic,
            content = article.content
        )
}