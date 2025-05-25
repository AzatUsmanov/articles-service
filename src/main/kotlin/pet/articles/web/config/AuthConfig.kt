package pet.articles.web.config

import com.auth0.jwt.interfaces.JWTVerifier
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import org.koin.ktor.ext.getProperty
import org.koin.ktor.ext.inject
import pet.articles.model.enums.AuthClaim
import pet.articles.service.user.UserService
import pet.articles.tool.exception.AuthenticationException

fun Application.configureAuth() {
    val realm: String = getProperty("jwt.realm")!!
    val userService: UserService by inject()
    val verifier: JWTVerifier by inject()

    install(Authentication) {
        jwt("auth-jwt") {
            this.realm = realm
            verifier(verifier)
            validate(buildJwtPayloadValidator(userService))
            challenge(buildJwtAuthChallenge())
        }
    }
}

fun buildJwtPayloadValidator(
    userService: UserService,
): ApplicationCall.(JWTCredential) -> Any? = { jwtCredential ->
        jwtCredential[AuthClaim.USERNAME.toString()]
            ?.takeIf { usernameClaimValue -> userService.existsByUsername(usernameClaimValue) }
            ?.let { JWTPrincipal(jwtCredential.payload) }
    }

fun buildJwtAuthChallenge(): JWTAuthChallengeFunction = { _, _ ->
    throw AuthenticationException("Token is not valid or has expired")
}

