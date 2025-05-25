package pet.articles.web.routing

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.getProperty
import org.koin.ktor.ext.inject
import pet.articles.model.dto.payload.AuthRequest
import pet.articles.model.dto.AuthResponse
import pet.articles.service.user.AuthService

fun Application.authRouting() {
    val path: String = getProperty("api.paths.auth")!!
    val service: AuthService by inject()
    routing {
        route(path) {
            authenticateRoute(service)
        }
    }
}

fun Route.authenticateRoute(service: AuthService) =
    post {
        val request: AuthRequest = call.receive()
        val response: AuthResponse = service.authenticate(request)
        call.respond(HttpStatusCode.OK, response)
    }
