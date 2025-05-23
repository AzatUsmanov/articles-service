package pet.articles.web.config

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import pet.articles.tool.exception.AccessDeniedException
import pet.articles.tool.exception.AuthenticationException
import pet.articles.tool.exception.DuplicateUserException
import pet.articles.tool.extension.toErrorResponse

fun Application.configureExceptionHandling() {
    install(StatusPages) {
        exception<Exception> { call, cause ->
            call.respond(HttpStatusCode.InternalServerError, cause.toErrorResponse())
        }
        exception<AuthenticationException> { call, cause ->
            call.respond(HttpStatusCode.Unauthorized, cause.toErrorResponse())
        }
        exception<AccessDeniedException> { call, cause ->
            call.respond(HttpStatusCode.Forbidden, cause.toErrorResponse())
        }
        exception<DuplicateUserException> { call, cause ->
            call.respond(HttpStatusCode.Conflict, cause.toErrorResponse())
        }
        exception<RequestValidationException> { call, cause ->
            call.respond(HttpStatusCode.UnprocessableEntity, cause.toErrorResponse())
        }
    }
}