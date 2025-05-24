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
import pet.articles.model.dto.payload.ReviewPayload
import pet.articles.service.ReviewService
import pet.articles.web.auth.getIdParam
import pet.articles.web.auth.plugin.withEditPermission
import pet.articles.web.auth.receiveReviewAuthorIdsFromBody

fun Application.reviewRouting() {
    val path: String = getProperty("api.paths.reviews")!!
    val service: ReviewService by inject()

    routing {
        authenticate("auth-jwt") {
            route(path) {
                findReviewByIdRoute(service)
                findReviewsByArticleIdRoute(service)
                findReviewsByAuthorIdRoute(service)

                withEditPermission(ApplicationCall::receiveReviewAuthorIdsFromBody) {
                    createReviewRoute(service)
                    deleteReviewByIdRoute(service)
                }
            }
        }
    }
}

fun Route.createReviewRoute(service: ReviewService) =
    post {
        val payloadForCreation: ReviewPayload = call.receive()
        val createdReview: Review = service.create(payloadForCreation.toReview())
        call.respond(HttpStatusCode.Created, createdReview)
    }

fun Route.deleteReviewByIdRoute(service: ReviewService) =
    delete("/{id}") {
        val id: Int = call.getIdParam()
        service.deleteById(id)
        call.respond(HttpStatusCode.NoContent)
    }

fun Route.findReviewByIdRoute(service: ReviewService) =
    get("/{id}") {
        val id: Int = call.getIdParam()
        service.findById(id)
            ?.let { review -> call.respond(HttpStatusCode.OK, review) }
            ?: call.respond(HttpStatusCode.NotFound)
    }

fun Route.findReviewsByArticleIdRoute(service: ReviewService) =
    get("/articles/{id}") {
        val articleId: Int = call.getIdParam()
        val foundReviews: List<Review> = service.findByArticleId(articleId)
        call.respond(HttpStatusCode.OK, foundReviews)
    }

fun Route.findReviewsByAuthorIdRoute(service: ReviewService) =
    get("/users/{id}") {
        val authorId: Int = call.getIdParam()
        val foundReviews: List<Review> = service.findByAuthorId(authorId)
        call.respond(HttpStatusCode.OK, foundReviews)
    }
