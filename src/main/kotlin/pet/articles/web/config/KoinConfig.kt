package pet.articles.web.config

import io.ktor.server.application.*
import org.koin.ktor.plugin.Koin
import pet.articles.config.configure

fun Application.configureKoin() {
    install(Koin) {
        configure()
    }
}