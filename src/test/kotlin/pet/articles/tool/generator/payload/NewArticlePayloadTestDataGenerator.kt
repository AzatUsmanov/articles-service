package pet.articles.tool.generator.payload

import pet.articles.model.dto.Article
import pet.articles.model.dto.User
import pet.articles.model.dto.payload.NewArticlePayload
import pet.articles.tool.generator.ArticleTestDataGenerator.Companion.generateSavedAuthorIds
import pet.articles.tool.generator.TestDataGenerator

class NewArticlePayloadTestDataGenerator(
    articleGenerator: TestDataGenerator<Article>,
    userGenerator: TestDataGenerator<User>
) : PayloadTestDataGeneratorBaseImpl<NewArticlePayload, Article>(
    convertToPayload = { article -> convertToNewArticlePayload(article, userGenerator) },
    modelTestDataGenerator = articleGenerator
) {

    companion object {
        private fun convertToNewArticlePayload(
            article: Article,
            userGenerator: TestDataGenerator<User>
        ) = NewArticlePayload(
            topic = article.topic,
            content = article.content,
            authorIds = generateSavedAuthorIds(userGenerator)
        )
    }
}