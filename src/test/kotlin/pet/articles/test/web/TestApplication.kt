package pet.articles.test.web

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.testing.*
import org.koin.core.Koin
import org.koin.test.KoinTest
import org.koin.test.inject
import pet.articles.model.dto.User
import pet.articles.model.dto.AuthResponse
import pet.articles.model.enums.UserRole
import pet.articles.service.AuthService
import pet.articles.test.tool.extension.toAuthRequest
import pet.articles.test.tool.producer.AuthenticationDetailsProducer
import pet.articles.web.config.configureAuth
import pet.articles.web.config.configureDoubleReceive
import pet.articles.web.config.configureExceptionHandling
import pet.articles.web.config.configureRouting
import pet.articles.web.config.configureSerialization
import pet.articles.web.config.configureValidation

fun Application.moduleWithoutKoin() {
    configureSerialization()
    configureValidation()
    configureDoubleReceive()
    configureAuth()
    configureRouting()
    configureExceptionHandling()
}

fun KoinTest.testApplicationAsUnauthorized(block: suspend (HttpClient) -> Unit) = testApplication {
    application {
        moduleWithoutKoin()
    }
    val client = createClient {
        install(ContentNegotiation) { json() }
        defaultRequest {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
        }
    }
    block(client)
}

fun KoinTest.testApplicationAsAuthorized(
    userRole: UserRole = UserRole.ROLE_ADMIN,
    block: suspend (client: HttpClient, authorizedUser: User) -> Unit
) = testApplication {
    val producer: AuthenticationDetailsProducer by inject()
    val service: AuthService by inject()
    val registeredUser: User = producer.produceRegisteredUserWithRawPassword(userRole)
    val response: AuthResponse = service.authenticate(registeredUser.toAuthRequest())

    application {
        moduleWithoutKoin()
    }
    val client = createClient {
        install(ContentNegotiation) {
            json()
        }
        defaultRequest {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer ${response.token}")
        }
    }
    block(client, registeredUser)
}