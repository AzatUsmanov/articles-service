package pet.articles.web.validation

import io.ktor.server.plugins.requestvalidation.*
import pet.articles.model.dto.payload.AuthRequest
import pet.articles.model.dto.payload.RegistrationPayload
import pet.articles.model.dto.payload.UserPayload
import pet.articles.model.enums.UserRole
import pet.articles.tool.extension.isValidEmail

import pet.articles.web.validation.UserPayloadValidation.ErrorMessages.EMAIL_INVALID_FORMAT
import pet.articles.web.validation.UserPayloadValidation.ErrorMessages.EMAIL_SIZE
import pet.articles.web.validation.UserPayloadValidation.ErrorMessages.PASSWORD_SIZE
import pet.articles.web.validation.UserPayloadValidation.ErrorMessages.USERNAME_SIZE
import pet.articles.web.validation.UserPayloadValidation.Fields.Constrains.EMAIL_MAX_LENGTH
import pet.articles.web.validation.UserPayloadValidation.Fields.Constrains.EMAIL_MIN_LENGTH
import pet.articles.web.validation.UserPayloadValidation.Fields.Constrains.PASSWORD_MAX_LENGTH
import pet.articles.web.validation.UserPayloadValidation.Fields.Constrains.PASSWORD_MIN_LENGTH
import pet.articles.web.validation.UserPayloadValidation.Fields.Constrains.USERNAME_MAX_LENGTH
import pet.articles.web.validation.UserPayloadValidation.Fields.Constrains.USERNAME_MIN_LENGTH
import pet.articles.web.validation.UserPayloadValidation.Fields.DefaultValues.EMAIL_VALUE
import pet.articles.web.validation.UserPayloadValidation.Fields.DefaultValues.ROLE_VALUE
import pet.articles.web.validation.UserPayloadValidation.Fields.Names.EMAIL
import pet.articles.web.validation.UserPayloadValidation.Fields.Names.PASSWORD
import pet.articles.web.validation.UserPayloadValidation.Fields.Names.USERNAME

object UserPayloadValidation {

    object Fields {

        object Names {
            const val USERNAME = "username"
            const val EMAIL = "email"
            const val PASSWORD = "password"
        }

        object Constrains {
            const val USERNAME_MIN_LENGTH = 5
            const val USERNAME_MAX_LENGTH = 30
            const val EMAIL_MIN_LENGTH = 5
            const val EMAIL_MAX_LENGTH = 50
            const val PASSWORD_MIN_LENGTH = 5
            const val PASSWORD_MAX_LENGTH = 50
        }

        object DefaultValues {
            const val EMAIL_VALUE = "email@mail.com"
            val ROLE_VALUE = UserRole.ROLE_USER
        }
    }

    object ErrorMessages {
        const val USERNAME_SIZE = "the length of the username must be between $USERNAME_MIN_LENGTH and $USERNAME_MAX_LENGTH"
        const val EMAIL_SIZE = "the length of the email must be between {min} and {max}"
        const val EMAIL_INVALID_FORMAT = "the email is in the wrong format"
        const val PASSWORD_SIZE = "the length of the password must be between {min} and {max}"
    }
}

fun UserPayload.validate(): ValidationResult {
    val reasons: MutableList<String> = ArrayList()

    validateUsername(username, reasons)
    validateEmail(email, reasons)
    validatePassword(password, reasons)

    return if (reasons.isEmpty()) {
        ValidationResult.Valid
    } else ValidationResult.Invalid(reasons)
}

fun RegistrationPayload.validate(): ValidationResult =
    UserPayload(username, email, password, ROLE_VALUE).validate()

fun AuthRequest.validate(): ValidationResult =
    RegistrationPayload(username, EMAIL_VALUE, password).validate()

fun validateUsername(username: String, reasons: MutableList<String>) {
    if (username.length !in USERNAME_MIN_LENGTH..USERNAME_MAX_LENGTH) {
        reasons.add("$USERNAME:$USERNAME_SIZE")
    }
}

fun validatePassword(password: String, reasons: MutableList<String>) {
    if (password.length !in PASSWORD_MIN_LENGTH..PASSWORD_MAX_LENGTH)  {
        reasons.add("$PASSWORD:$PASSWORD_SIZE")
    }
}

fun validateEmail(email: String, reasons: MutableList<String>) {
    if (!email.isValidEmail()) {
        reasons.add("$EMAIL:$EMAIL_INVALID_FORMAT")
    }
    if (email.length !in EMAIL_MIN_LENGTH..EMAIL_MAX_LENGTH) {
        reasons.add("$EMAIL:$EMAIL_SIZE")
    }
}
