package pet.articles.web.auth

import io.ktor.server.application.*

fun ApplicationCall.getIdParam(): Int = parameters["id"]!!.toInt()
