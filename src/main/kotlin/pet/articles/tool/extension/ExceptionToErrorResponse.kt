package pet.articles.tool.extension

import io.ktor.server.plugins.requestvalidation.*
import pet.articles.model.dto.ErrorResponse
import pet.articles.model.enums.ErrorResponseType
import pet.articles.tool.exception.AuthenticationException

fun Exception.toErrorResponse(): ErrorResponse =
    ErrorResponse(message.orEmpty())

fun AuthenticationException.toErrorResponse(): ErrorResponse =
    ErrorResponse(message.orEmpty(), ErrorResponseType.AUTHENTICATION)

fun RequestValidationException.toErrorResponse(): ErrorResponse =
    ErrorResponse(
        message.orEmpty(),
        ErrorResponseType.VALIDATION,
        reasons.associate {
            val (key, value) = it.split(":")
            key to value
        }
    )