package pet.articles.web.routing

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.getProperty
import org.koin.ktor.ext.inject
import pet.articles.model.dto.User
import pet.articles.model.dto.payload.RegistrationPayload
import pet.articles.service.user.RegistrationService

fun Application.registrationRouting() {
    val path: String = getProperty("api.paths.registration")!!
    val service: RegistrationService by inject()
    routing {
        route(path) {
            registerUserRoute(service)
        }
    }
}

fun Route.registerUserRoute(service: RegistrationService) =
    post {
        val payloadForRegistration: RegistrationPayload = call.receive()
        val registeredUser: User = service.register(payloadForRegistration.toUser())
        call.respond(HttpStatusCode.Created, registeredUser)
    }
