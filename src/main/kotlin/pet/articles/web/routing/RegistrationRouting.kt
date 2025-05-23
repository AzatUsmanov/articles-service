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
import pet.articles.model.dto.payload.RegistrationPayload
import pet.articles.model.dto.payload.UserPayload
import pet.articles.model.enums.UserRole
import pet.articles.service.RegistrationService
import pet.articles.service.UserService
import pet.articles.web.auth.EditPermissionPlugin
import pet.articles.web.auth.RBACPlugin

fun Application.registrationRouting() {
    val path: String = getProperty("api.paths.registration")!!
    val registrationService: RegistrationService by inject()
    routing {
        route(path) {
            registerUserRoute(registrationService)
        }
    }
}

fun Route.registerUserRoute(registrationService: RegistrationService) =
    post {
        val registrationPayload: RegistrationPayload = call.receive()
        val registeredUser: User = registrationService.register(registrationPayload.toUser())
        call.respond(HttpStatusCode.Created, registeredUser)
    }
