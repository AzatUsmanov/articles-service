package pet.articles.web.config

import io.ktor.server.application.*
import pet.articles.web.routing.articleRouting
import pet.articles.web.routing.authRouting
import pet.articles.web.routing.registrationRouting
import pet.articles.web.routing.reviewRouting
import pet.articles.web.routing.userRouting

fun Application.configureRouting() {
    registrationRouting()
    authRouting()
    userRouting()
    articleRouting()
    reviewRouting()
}