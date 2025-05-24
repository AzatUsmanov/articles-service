package pet.articles.web

import io.ktor.server.application.*
import pet.articles.web.config.configureAuth
import pet.articles.web.config.configureDoubleReceive
import pet.articles.web.config.configureExceptionHandling
import pet.articles.web.config.configureKoin
import pet.articles.web.config.configureRouting
import pet.articles.web.config.configureSerialization
import pet.articles.web.config.configureValidation

fun Application.module() {
    configureKoin()
    configureSerialization()
    configureValidation()
    configureDoubleReceive()
    configureAuth()
    configureRouting()
    configureExceptionHandling()
}