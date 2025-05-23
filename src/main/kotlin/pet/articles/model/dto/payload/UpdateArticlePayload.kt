package pet.articles.model.dto.payload

import kotlinx.serialization.Serializable
import pet.articles.model.dto.Article

import java.time.LocalDateTime

@Serializable
data class UpdateArticlePayload(
    val topic: String,
    val content: String
) {
    fun toArticle(): Article = Article(
        id = null,
        topic = topic,
        content = content,
        dateOfCreation = LocalDateTime.now()
    )
}