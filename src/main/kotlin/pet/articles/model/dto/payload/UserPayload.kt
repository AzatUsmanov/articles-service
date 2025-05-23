package pet.articles.model.dto.payload

import kotlinx.serialization.Serializable

import pet.articles.model.dto.User
import pet.articles.model.enums.UserRole

@Serializable
data class UserPayload(
    val username : String,
    val email : String,
    val password : String,
    val role : UserRole
) {
    fun toUser(): User = User(
        id = null,
        username = username,
        email = email,
        password = password,
        role = role
    )
}


