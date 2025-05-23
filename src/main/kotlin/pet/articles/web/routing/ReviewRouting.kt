package pet.articles.web.routing

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.getProperty
import org.koin.ktor.ext.inject
import pet.articles.model.dto.Review
import pet.articles.model.dto.payload.NewArticlePayload
import pet.articles.model.dto.payload.ReviewPayload
import pet.articles.service.ReviewService
import pet.articles.service.UserService
import pet.articles.tool.extension.getIdParam
import pet.articles.web.auth.withEditPermission

fun Application.reviewRouting() {
    val path: String = getProperty("api.paths.reviews")!!
    val reviewService: ReviewService by inject()

    routing {
        authenticate("auth-jwt") {
            route(path) {
                findReviewByIdRoute(reviewService)
                findReviewsByArticleIdRoute(reviewService)
                findReviewsByAuthorIdRoute(reviewService)

                withEditPermission(ApplicationCall::receiveReviewAuthorIdsFromBody) {
                    createReviewRoute(reviewService)
                    deleteReviewByIdRoute(reviewService)
                }
            }
        }
    }
}

fun Route.createReviewRoute(reviewService: ReviewService) =
    post {
        val payload: ReviewPayload = call.receive()
        val createdReview: Review = reviewService.create(payload.toReview())
        call.respond(HttpStatusCode.Created, createdReview)
    }

fun Route.deleteReviewByIdRoute(reviewService: ReviewService) =
    delete("/{id}") {
        val id: Int = call.getIdParam()
        reviewService.deleteById(id)
        call.respond(HttpStatusCode.NoContent)
    }

fun Route.findReviewByIdRoute(reviewService: ReviewService) =
    get("/{id}") {
        val id: Int = call.getIdParam()
        reviewService.findById(id)?.let { review ->
            call.respond(HttpStatusCode.OK, review)
        } ?: call.respond(HttpStatusCode.NotFound)
    }

fun Route.findReviewsByArticleIdRoute(reviewService: ReviewService) =
    get("/articles/{id}") {
        val articleId: Int = call.getIdParam()
        val reviews: List<Review> = reviewService.findByArticleId(articleId)
        call.respond(HttpStatusCode.OK, reviews)
    }

fun Route.findReviewsByAuthorIdRoute(reviewService: ReviewService) =
    get("/users/{id}") {
        val authorId: Int = call.getIdParam()
        val reviews: List<Review> = reviewService.findByAuthorId(authorId)
        call.respond(HttpStatusCode.OK, reviews)
    }

suspend fun ApplicationCall.receiveReviewAuthorIdsFromBody(): List<Int> {
    val service: ReviewService by inject()
    return parameters["id"]
        ?.toInt()
        ?.let { reviewId -> service.findById(reviewId) ?: return emptyList() }
        ?.let { review -> listOf(review.authorId!!) }
        ?: listOf(receive<ReviewPayload>().authorId)
}
