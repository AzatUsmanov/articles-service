package pet.articles.model.dto.payload

import kotlinx.serialization.Serializable
import pet.articles.model.dto.User
import pet.articles.model.enums.UserRole

@Serializable
data class RegistrationPayload(
    val username : String,
    val email : String,
    val password : String
) {
    fun toUser(): User = User(
        id = null,
        username = username,
        email = email,
        password = password,
        role = UserRole.ROLE_USER
    )
}
