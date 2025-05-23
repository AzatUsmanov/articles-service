package pet.articles.web.config

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.doublereceive.*

fun Application.configureDoubleReceive() {
    install(DoubleReceive) {
        cacheRawRequest = false
    }
}