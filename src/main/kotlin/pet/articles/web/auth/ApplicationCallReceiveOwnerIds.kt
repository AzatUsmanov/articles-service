package pet.articles.web.auth

import io.ktor.server.application.*
import io.ktor.server.request.*
import org.koin.ktor.ext.inject
import pet.articles.model.dto.payload.NewArticlePayload
import pet.articles.model.dto.payload.ReviewPayload
import pet.articles.service.ReviewService
import pet.articles.service.UserService

fun ApplicationCall.receiveUserOwnerIdsFromPath(): List<Int> =
    listOf(getIdParam())

suspend fun ApplicationCall.receiveArticleAuthorIdsFromBody(): List<Int> {
    val service: UserService by inject()
    return parameters["id"]
        ?.toInt()
        ?.let { articleId -> service.findAuthorIdsByArticleId(articleId) }
        ?: receive<NewArticlePayload>().authorIds
}

suspend fun ApplicationCall.receiveReviewAuthorIdsFromBody(): List<Int> {
    val service: ReviewService by inject()
    return parameters["id"]
        ?.toInt()
        ?.let { reviewId -> service.findById(reviewId) ?: return emptyList() }
        ?.let { review -> listOf(review.authorId!!) }
        ?: listOf(receive<ReviewPayload>().authorId)
}
