package pet.articles.web.auth.plugin

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import pet.articles.model.enums.AuthClaim
import pet.articles.model.enums.UserRole
import pet.articles.tool.exception.AccessDeniedException
import pet.articles.tool.exception.AuthenticationException
import pet.articles.tool.extension.get

class EditPermissionPluginConfig {
    lateinit var receiveOwnerIdsFromBody: suspend ApplicationCall.() -> List<Int>
}

val EditPermissionPlugin = createRouteScopedPlugin(
    name = "EditPermissionPlugin",
    createConfiguration = ::EditPermissionPluginConfig
) {
    on(AuthenticationChecked) check@ { call ->
        if (call.request.httpMethod == HttpMethod.Get) return@check

        val principal = call.principal<JWTPrincipal>()
            ?: throw AuthenticationException("User not authenticated")

        val authorsIds: List<Int> = pluginConfig.receiveOwnerIdsFromBody(call)
        val currentUserId = principal[AuthClaim.ID].toInt()
        val currentUsername: String = principal[AuthClaim.USERNAME]
        val userRoleName: String = principal[AuthClaim.ROLE]

        if (userRoleName == UserRole.ROLE_USER.name && currentUserId !in authorsIds) {
            throw AccessDeniedException(
                "User with username = $currentUsername doesn't have the required permissions"
            )
        }
    }
}

fun Route.withEditPermission(
    receiveOwnerIdsFromBody: suspend ApplicationCall.() -> List<Int>,
    body: Route.() -> Unit
) {
    install(EditPermissionPlugin) {
        this.receiveOwnerIdsFromBody = receiveOwnerIdsFromBody
    }
    body()
}
