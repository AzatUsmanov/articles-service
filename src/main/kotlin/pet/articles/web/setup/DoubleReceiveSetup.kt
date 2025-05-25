package pet.articles.web.setup

import io.ktor.server.application.*
import io.ktor.server.plugins.doublereceive.*

fun Application.configureDoubleReceive() {
    install(DoubleReceive) {
        cacheRawRequest = false
    }
}