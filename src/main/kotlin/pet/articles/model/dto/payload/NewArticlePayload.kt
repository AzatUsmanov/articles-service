package pet.articles.model.dto.payload

import kotlinx.serialization.Serializable

import pet.articles.model.dto.Article

import java.time.LocalDateTime


@Serializable
data class NewArticlePayload(
    val topic: String,
    val content: String,
    val authorIds: List<Int>
) {
    fun toArticle(): Article = Article(
        id = null,
        topic = topic,
        content = content,
        dateOfCreation = LocalDateTime.now()
    )
}

