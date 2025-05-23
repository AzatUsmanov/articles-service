package pet.articles.model.dto

import com.fasterxml.jackson.annotation.JsonIgnore
import kotlinx.serialization.Serializable

import pet.articles.model.enums.UserRole

@Serializable
data class User(
    val id : Int?,
    val username : String,
    val email : String,
    val role : UserRole,
    @JsonIgnore val password: String?
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is User) return false

        if (id != other.id) return false
        if (username != other.username) return false
        if (email != other.email) return false
        if (role != other.role) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id ?: 0
        result = 31 * result + username.hashCode()
        result = 31 * result + email.hashCode()
        result = 31 * result + role.hashCode()
        return result
    }

    override fun toString(): String =
        "User(email='$email', id=$id, role=$role, username='$username')"
}
