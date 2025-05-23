package pet.articles.web.config

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.JWTVerifier
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.getProperty
import org.koin.ktor.ext.inject
import pet.articles.model.enums.AuthClaim
import pet.articles.service.UserService
import pet.articles.tool.exception.AuthenticationException
import java.util.*

fun Application.configureAuth() {
    val secret: String = getProperty("jwt.secret")!!
    val issuer: String = getProperty("jwt.issuer")!!
    val audience: String = getProperty("jwt.audience")!!
    val realm: String = getProperty("jwt.realm")!!
    val userService: UserService by inject()

    install(Authentication) {
        jwt("auth-jwt") {
            this.realm = realm
            verifier(buildVerifier(secret, audience, issuer))
            validate(buildJwtPayloadValidator(userService))
            challenge(buildJwtAuthChallenge())
        }
    }
}

fun buildVerifier(secret: String, audience: String, issuer: String): JWTVerifier =
    JWT
        .require(Algorithm.HMAC256(secret))
        .withAudience(audience)
        .withIssuer(issuer)
        .build()

fun buildJwtPayloadValidator(
    userService: UserService,
): ApplicationCall.(JWTCredential) -> Any? = validator@ { jwtCredential ->
    val usernameClaimValue: String = jwtCredential[AuthClaim.USERNAME.toString()]
        ?: return@validator null
    if (userService.existsByUsername(usernameClaimValue)) {
        JWTPrincipal(jwtCredential.payload)
    } else null
}

fun buildJwtAuthChallenge(): JWTAuthChallengeFunction = { _, _ ->
    throw AuthenticationException("Token is not valid or has expired")
}

