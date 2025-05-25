package pet.articles.web.setup

import io.ktor.server.application.*
import org.koin.ktor.plugin.Koin
import pet.articles.config.di.configure

fun Application.configureKoin() {
    install(Koin) {
        configure()
    }
}