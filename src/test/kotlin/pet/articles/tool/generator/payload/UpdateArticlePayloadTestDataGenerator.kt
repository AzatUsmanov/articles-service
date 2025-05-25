package pet.articles.tool.generator.payload

import pet.articles.model.dto.Article
import pet.articles.model.dto.payload.UpdateArticlePayload
import pet.articles.tool.generator.TestDataGenerator

class UpdateArticlePayloadTestDataGenerator(
    articleGenerator: TestDataGenerator<Article>
) : PayloadTestDataGeneratorBaseImpl<UpdateArticlePayload, Article>(
    convertToPayload = ::convertToUpdateArticlePayload,
    modelTestDataGenerator = articleGenerator
) {

    companion object {
        private fun convertToUpdateArticlePayload(article: Article): UpdateArticlePayload =
            UpdateArticlePayload(
                topic = article.topic,
                content = article.content
            )
    }
}