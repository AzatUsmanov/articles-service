package pet.articles.web.routing

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.getProperty
import org.koin.ktor.ext.inject
import pet.articles.model.dto.Article
import pet.articles.model.dto.payload.NewArticlePayload
import pet.articles.model.dto.payload.UpdateArticlePayload
import pet.articles.service.ArticleService
import pet.articles.web.auth.getIdParam
import pet.articles.web.auth.plugin.withEditPermission
import pet.articles.web.auth.receiveArticleAuthorIdsFromBody

fun Application.articleRouting() {
    val path: String = getProperty("api.paths.articles")!!
    val service: ArticleService by inject()
    routing {
        authenticate("auth-jwt") {
            route(path) {
                findArticleByIdRoute(service)
                findAllArticlesRoute(service)
                findArticlesByAuthorIdRoute(service)

                withEditPermission(ApplicationCall::receiveArticleAuthorIdsFromBody) {
                    createArticleRoute(service)
                    updateArticleByIdRoute(service)
                    deleteArticleByIdRoute(service)
                }
            }
        }
    }
}

fun Route.createArticleRoute(service: ArticleService) =
    post {
        val payloadForCreation: NewArticlePayload = call.receive()
        val createdArticle: Article = service.create(
            payloadForCreation.toArticle(),
            payloadForCreation.authorIds
        )
        call.respond(HttpStatusCode.Created, createdArticle)
    }

fun Route.updateArticleByIdRoute(service: ArticleService) =
    patch("/{id}") {
        val id: Int = call.getIdParam()
        val payloadForUpdate: UpdateArticlePayload = call.receive()
        val updatedArticle: Article = service.updateById(payloadForUpdate.toArticle(), id)
        call.respond(HttpStatusCode.OK, updatedArticle)
    }

fun Route.deleteArticleByIdRoute(service: ArticleService) =
    delete("/{id}") {
        val id: Int = call.getIdParam()
        service.deleteById(id)
        call.respond(HttpStatusCode.NoContent)
    }

fun Route.findArticleByIdRoute(service: ArticleService) =
    get("/{id}") {
        val id: Int = call.getIdParam()
        service.findById(id)
            ?.let { foundArticle -> call.respond(HttpStatusCode.OK, foundArticle) }
            ?: call.respond(HttpStatusCode.NotFound)
    }

fun Route.findArticlesByAuthorIdRoute(service: ArticleService) =
    get("/authorship/{id}") {
        val authorId: Int = call.getIdParam()
        val foundArticles: List<Article> = service.findArticlesByAuthorId(authorId)
        call.respond(HttpStatusCode.OK, foundArticles)
    }

fun Route.findAllArticlesRoute(service: ArticleService) =
    get {
        val foundArticles: List<Article> = service.findAll()
        call.respond(HttpStatusCode.OK, foundArticles)
    }

