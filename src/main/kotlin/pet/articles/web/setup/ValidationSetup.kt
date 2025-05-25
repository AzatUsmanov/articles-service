package pet.articles.web.setup

import io.ktor.server.application.*
import io.ktor.server.plugins.requestvalidation.*
import pet.articles.model.dto.payload.AuthRequest
import pet.articles.model.dto.payload.NewArticlePayload
import pet.articles.model.dto.payload.RegistrationPayload
import pet.articles.model.dto.payload.ReviewPayload
import pet.articles.model.dto.payload.UpdateArticlePayload
import pet.articles.model.dto.payload.UserPayload
import pet.articles.web.validation.validate


fun Application.configureValidation() {
    install(RequestValidation) {
        validate(AuthRequest::validate)
        validate(RegistrationPayload::validate)
        validate(UserPayload::validate)

        validate(NewArticlePayload::validate)
        validate(UpdateArticlePayload::validate)

        validate(ReviewPayload::validate)
    }
}




