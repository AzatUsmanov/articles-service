package pet.articles.model.dto.payload

import kotlinx.serialization.Serializable

@Serializable
data class AuthRequest(val username: String, val password: String)
