package pet.articles.model.dto.payload;

import kotlinx.serialization.Serializable
import pet.articles.model.dto.Review
import pet.articles.model.enums.ReviewType

import java.time.LocalDateTime


@Serializable
data class ReviewPayload(
    val type: ReviewType,
    val content: String,
    val authorId: Int,
    val articleId: Int
) {
    fun toReview(): Review = Review(
        id = null,
        type = type,
        content = content,
        authorId = authorId,
        articleId = articleId,
        dateOfCreation = LocalDateTime.now()
    )
}