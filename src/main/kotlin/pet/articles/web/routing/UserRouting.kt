package pet.articles.web.routing

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jooq.impl.QOM
import org.koin.ktor.ext.getProperty
import org.koin.ktor.ext.inject
import pet.articles.model.dto.User
import pet.articles.model.dto.payload.NewArticlePayload
import pet.articles.model.dto.payload.UserPayload
import pet.articles.model.enums.UserRole
import pet.articles.service.UserService
import pet.articles.tool.extension.getIdParam
import pet.articles.web.auth.EditPermissionPlugin
import pet.articles.web.auth.RBACPlugin
import pet.articles.web.auth.withEditPermission
import pet.articles.web.auth.withRole

fun Application.userRouting() {
    val path: String = getProperty("api.paths.users")!!
    val userService: UserService by inject()
    routing {
        authenticate("auth-jwt") {
            route(path) {
                findUserByIdRoute(userService)
                findAllUsersRoute(userService)
                findAuthorsByArticleIdRoute(userService)
            }
            route("$path/{id}") {
                withEditPermission(ApplicationCall::receiveUserOwnerIdsFromPath) {
                    updateUserByIdRoute(userService)
                    deleteUserByIdRoute(userService)
                }
            }
            route("$path/admin") {
                withRole(UserRole.ROLE_ADMIN) {
                    createUserRoute(userService)
                }
            }
        }
    }
}

fun Route.createUserRoute(userService: UserService) =
    post {
        val userPayload: UserPayload = call.receive()
        val savedUser: User = userService.create(userPayload.toUser())
        call.respond(HttpStatusCode.Created, savedUser)
    }

fun Route.updateUserByIdRoute(userService: UserService) =
    patch {
        val id: Int = call.getIdParam()
        val userPayload: UserPayload = call.receive()
        val updatedUser: User = userService.updateById(userPayload.toUser(), id)
        call.respond(HttpStatusCode.OK, updatedUser)
    }


fun Route.deleteUserByIdRoute(userService: UserService) =
    delete {
        val id: Int = call.getIdParam()
        userService.deleteById(id)
        call.respond(HttpStatusCode.NoContent)
    }

fun Route.findUserByIdRoute(userService: UserService) =
    get("/{id}") {
        val id: Int = call.getIdParam()
        userService.findById(id)?.let { user ->
            call.respond(HttpStatusCode.OK, user)
        } ?: call.respond(HttpStatusCode.NotFound)
    }

fun Route.findAuthorsByArticleIdRoute(userService: UserService) =
    get("/authorship/{id}") {
        val articleId: Int = call.getIdParam()
        val authors: List<User> = userService.findAuthorsByArticleId(articleId)
        call.respond(HttpStatusCode.OK, authors)
    }

fun Route.findAllUsersRoute(userService: UserService) =
    get {
        val users: List<User> = userService.findAll()
        call.respond(HttpStatusCode.OK, users)
    }

fun ApplicationCall.receiveUserOwnerIdsFromPath(): List<Int> =
    listOf(getIdParam())
