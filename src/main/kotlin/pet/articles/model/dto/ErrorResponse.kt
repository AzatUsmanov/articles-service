package pet.articles.model.dto

import kotlinx.serialization.Serializable
import pet.articles.model.enums.ErrorResponseType

@Serializable
data class ErrorResponse(
    val message: String,
    val errorResponseType: ErrorResponseType = ErrorResponseType.COMMON,
    val details: Map<String, String> = emptyMap()
)