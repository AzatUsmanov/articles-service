package pet.articles.web.auth.plugin

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.routing.*
import pet.articles.model.enums.AuthClaim
import pet.articles.tool.extension.get

import pet.articles.model.enums.UserRole
import pet.articles.tool.exception.AccessDeniedException
import pet.articles.tool.exception.AuthenticationException

class RbacPluginConfig {
    var roles: List<UserRole> = emptyList()
}

val RbacPlugin = createRouteScopedPlugin(
    name = "RoleBasedAccessControlPlugin",
    createConfiguration = ::RbacPluginConfig
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

fun Route.withRole(vararg roles: UserRole, body: Route.() -> Unit) {
    install(RbacPlugin) {
        this.roles = roles.toList()
    }
    body()
}