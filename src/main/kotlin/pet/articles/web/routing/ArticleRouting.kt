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
import pet.articles.service.UserService
import pet.articles.tool.extension.getIdParam
import pet.articles.web.auth.withEditPermission

fun Application.articleRouting() {
    val path: String = getProperty("api.paths.articles")!!
    val articleService: ArticleService by inject()

    routing {
        authenticate("auth-jwt") {
            route(path) {
                findArticleByIdRoute(articleService)
                findAllArticlesRoute(articleService)
                findArticlesByAuthorIdRoute(articleService)

                withEditPermission(ApplicationCall::receiveArticleAuthorIdsFromBody) {
                    createArticleRoute(articleService)
                    updateArticleByIdRoute(articleService)
                    deleteArticleByIdRoute(articleService)
                }
            }
        }
    }
}

fun Route.createArticleRoute(articleService: ArticleService) =
    post {
        val payload: NewArticlePayload = call.receive()
        val createdArticle: Article = articleService.create(
            payload.toArticle(),
            payload.authorIds
        )
        call.respond(HttpStatusCode.Created, createdArticle)
    }

fun Route.updateArticleByIdRoute(articleService: ArticleService) =
    patch("/{id}") {
        val id: Int = call.getIdParam()
        val payload: UpdateArticlePayload = call.receive()
        val updatedArticle: Article = articleService.updateById(
            payload.toArticle(),
            id
        )
        call.respond(HttpStatusCode.OK, updatedArticle)
    }

fun Route.deleteArticleByIdRoute(articleService: ArticleService) =
    delete("/{id}") {
        val id: Int = call.getIdParam()
        articleService.deleteById(id)
        call.respond(HttpStatusCode.NoContent)
    }

fun Route.findArticleByIdRoute(articleService: ArticleService) =
    get("/{id}") {
        val id: Int = call.getIdParam()
        articleService.findById(id)?.let { article ->
            call.respond(HttpStatusCode.OK, article)
        } ?: call.respond(HttpStatusCode.NotFound)
    }

fun Route.findArticlesByAuthorIdRoute(articleService: ArticleService) =
    get("/authorship/{id}") {
        val authorId: Int = call.getIdParam()
        val articles: List<Article> = articleService.findArticlesByAuthorId(authorId)
        call.respond(HttpStatusCode.OK, articles)
    }

fun Route.findAllArticlesRoute(articleService: ArticleService) =
    get {
        val articles: List<Article> = articleService.findAll()
        call.respond(HttpStatusCode.OK, articles)
    }

suspend fun ApplicationCall.receiveArticleAuthorIdsFromBody(): List<Int> {
    val service: UserService by inject()
    return parameters["id"]
        ?.toInt()
        ?.let { articleId -> service.findAuthorIdsByArticleId(articleId) }
        ?: receive<NewArticlePayload>().authorIds
}
