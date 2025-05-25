package pet.articles.tool.extension

import io.ktor.server.auth.jwt.*
import pet.articles.model.enums.AuthClaim

operator fun JWTPrincipal.get(claim: AuthClaim): String =
    get(claim.name)!!