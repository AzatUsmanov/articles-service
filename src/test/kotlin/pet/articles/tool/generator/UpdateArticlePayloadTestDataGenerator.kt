package pet.articles.tool.generator

import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

import pet.articles.model.dto.Article
import pet.articles.model.dto.payload.UpdateArticlePayload

class UpdateArticlePayloadTestDataGenerator(
    private val articleGenerator: TestDataGenerator<Article>
) : TestDataGenerator<UpdateArticlePayload> {

    override fun generateSavedData(dataSize: Int): List<UpdateArticlePayload> =
        articleGenerator.generateSavedData(dataSize).map(::convertToUpdateArticlePayload)

    override fun generateUnsavedData(dataSize: Int): List<UpdateArticlePayload> =
        articleGenerator.generateUnsavedData(dataSize).map(::convertToUpdateArticlePayload)

    override fun generateInvalidData(): UpdateArticlePayload =
        convertToUpdateArticlePayload(articleGenerator.generateInvalidData())

    private fun convertToUpdateArticlePayload(article: Article): UpdateArticlePayload =
        UpdateArticlePayload(
            topic = article.topic,
            content = article.content
        )
}