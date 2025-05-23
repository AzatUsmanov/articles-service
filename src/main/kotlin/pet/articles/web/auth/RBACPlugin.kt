package pet.articles.web.auth

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.routing.*
import pet.articles.model.enums.AuthClaim
import pet.articles.tool.extension.get

import pet.articles.model.enums.UserRole
import pet.articles.tool.exception.AccessDeniedException
import pet.articles.tool.exception.AuthenticationException


val RBACPlugin = createRouteScopedPlugin(
    name = "RoleBasedAccessControlPlugin",
    createConfiguration = ::RBACPluginConfig
) {
    on(AuthenticationChecked) { call ->
        val principal = call.principal<JWTPrincipal>()
            ?: throw AuthenticationException("User not authenticated")
        val currentUsername: String = principal[AuthClaim.USERNAME]
        val userRoleName: String = principal[AuthClaim.ROLE]

        if (UserRole.valueOf(userRoleName) !in pluginConfig.roles) {
            throw AccessDeniedException(
                "User with username = $currentUsername does not have the required permissions"
            )
        }
    }
}

class RBACPluginConfig {
    var roles: List<UserRole> = emptyList()
}

fun Route.withRole(vararg roles: UserRole, body: Route.() -> Unit) {
    install(RBACPlugin) {
        this.roles = roles.toList()
    }
    body()
}