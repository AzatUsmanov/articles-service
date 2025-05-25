package pet.articles.web.routing

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.getProperty
import org.koin.ktor.ext.inject
import pet.articles.model.dto.User
import pet.articles.model.dto.payload.UserPayload
import pet.articles.model.enums.UserRole
import pet.articles.service.user.UserService
import pet.articles.tool.extension.getIdParam
import pet.articles.web.auth.plugin.withEditPermission
import pet.articles.web.auth.plugin.withRole
import pet.articles.web.auth.receiveUserOwnerIdsFromPath

fun Application.userRouting() {
    val path: String = getProperty("api.paths.users")!!
    val adminPath: String = getProperty("api.paths.users.admin")!!
    val userService: UserService by inject()
    routing {
        authenticate("auth-jwt") {
            route(path) {
                findUserByIdRoute(userService)
                findAllUsersRoute(userService)
                findAuthorsByArticleIdRoute(userService)
            }
            route("$path/{id}") {
                withEditPermission(
                    ApplicationCall::receiveUserOwnerIdsFromPath
                ) {
                    updateUserByIdRoute(userService)
                    deleteUserByIdRoute(userService)
                }
            }
            route(adminPath) {
                withRole(UserRole.ROLE_ADMIN) {
                    createUserRoute(userService)
                }
            }
        }
    }
}

fun Route.createUserRoute(service: UserService) =
    post {
        val payloadForCreation: UserPayload = call.receive()
        val savedUser: User = service.create(payloadForCreation.toUser())
        call.respond(HttpStatusCode.Created, savedUser)
    }

fun Route.updateUserByIdRoute(service: UserService) =
    patch {
        val id: Int = call.getIdParam()
        val payloadForUpdate: UserPayload = call.receive()
        val updatedUser: User = service.updateById(
            payloadForUpdate.toUser(),
            id
        )
        call.respond(HttpStatusCode.OK, updatedUser)
    }


fun Route.deleteUserByIdRoute(service: UserService) =
    delete {
        val id: Int = call.getIdParam()
        service.deleteById(id)
        call.respond(HttpStatusCode.NoContent)
    }

fun Route.findUserByIdRoute(service: UserService) =
    get("/{id}") {
        val id: Int = call.getIdParam()
        service.findById(id)
            ?.let { user -> call.respond(HttpStatusCode.OK, user) }
            ?: call.respond(HttpStatusCode.NotFound)
    }

fun Route.findAuthorsByArticleIdRoute(service: UserService) =
    get("/authorship/{id}") {
        val articleId: Int = call.getIdParam()
        val foundAuthors: List<User> = service.findAuthorsByArticleId(articleId)
        call.respond(HttpStatusCode.OK, foundAuthors)
    }

fun Route.findAllUsersRoute(service: UserService) =
    get {
        val foundUsers: List<User> = service.findAll()
        call.respond(HttpStatusCode.OK, foundUsers)
    }
