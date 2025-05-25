package pet.articles.web

import io.ktor.server.application.*
import pet.articles.web.setup.configureAuth
import pet.articles.web.setup.configureDoubleReceive
import pet.articles.web.setup.configureExceptionHandling
import pet.articles.web.setup.configureRouting
import pet.articles.web.setup.configureSerialization
import pet.articles.web.setup.configureValidation

fun Application.testModule() {
    configureSerialization()
    configureValidation()
    configureDoubleReceive()
    configureAuth()
    configureRouting()
    configureExceptionHandling()
}
