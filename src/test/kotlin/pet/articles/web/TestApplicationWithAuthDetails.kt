package pet.articles.web

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import org.koin.test.KoinTest
import org.koin.test.inject
import pet.articles.model.dto.User
import pet.articles.model.dto.AuthResponse
import pet.articles.model.enums.UserRole
import pet.articles.service.user.AuthService
import pet.articles.tool.extension.toAuthRequest
import pet.articles.tool.producer.AuthenticationDetailsProducer


fun KoinTest.testApplicationAsUnauthorized(block: suspend (HttpClient) -> Unit) = testApplication {
    application {
        testModule()
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
        testModule()
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