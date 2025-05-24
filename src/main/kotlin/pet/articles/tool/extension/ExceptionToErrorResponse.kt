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
        message = message.orEmpty(),
        errorResponseType = ErrorResponseType.VALIDATION,
        details = reasons.associate { reason ->
            val (key, value) = reason.split(":")
            key to value
        }
    )