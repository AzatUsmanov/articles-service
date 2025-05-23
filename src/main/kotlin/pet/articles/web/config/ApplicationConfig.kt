package pet.articles.web.config

import io.ktor.server.application.*

fun Application.module() {
    configureKoin()
    configureSerialization()
    configureValidation()
    configureDoubleReceive()
    configureAuth()
    configureRouting()
    configureExceptionHandling()
}