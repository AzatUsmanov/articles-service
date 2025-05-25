package pet.articles.tool.extension

import io.ktor.server.application.*

fun ApplicationCall.getIdParam(): Int =
    parameters["id"]!!.toInt()
