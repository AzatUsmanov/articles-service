package pet.articles.web.validation

import io.ktor.server.plugins.requestvalidation.*
import pet.articles.model.dto.payload.ReviewPayload
import pet.articles.web.validation.ReviewValidation.Fields.Constraints.ARTICLE_ID_MIN_VALUE
import pet.articles.web.validation.ReviewValidation.Fields.Constraints.AUTHOR_ID_MIN_VALUE
import pet.articles.web.validation.ReviewValidation.Fields.Constraints.CONTENT_MAX_LENGTH
import pet.articles.web.validation.ReviewValidation.Fields.Constraints.CONTENT_MIN_LENGTH
import pet.articles.web.validation.ReviewValidation.Fields.Names.ARTICLE_ID
import pet.articles.web.validation.ReviewValidation.Fields.Names.AUTHOR_ID
import pet.articles.web.validation.ReviewValidation.Fields.Names.CONTENT

object ReviewValidation {

    object Fields {

        object Names {
            const val CONTENT = "content"
            const val AUTHOR_ID = "authorId"
            const val ARTICLE_ID = "articleId"
        }

        object Constraints {
            const val AUTHOR_ID_MIN_VALUE = 1
            const val ARTICLE_ID_MIN_VALUE = 1
            const val CONTENT_MIN_LENGTH = 1
            const val CONTENT_MAX_LENGTH = 500
        }
    }

    object ErrorMessages {
        const val CONTENT_SIZE = "the length of the content must be between $CONTENT_MIN_LENGTH and $CONTENT_MAX_LENGTH"
        const val AUTHOR_ID_POSITIVE = "authorId must be positive"
        const val ARTICLE_ID_POSITIVE = "articleId must be positive"
    }
}

fun ReviewPayload.validate(): ValidationResult {
    val reasons: MutableList<String> = ArrayList()

    validateContent(content, reasons)
    validateAuthorId(authorId, reasons)
    validateArticleId(articleId, reasons)

    return if (reasons.isEmpty()) {
        ValidationResult.Valid
    } else ValidationResult.Invalid(reasons)
}

private fun validateContent(content: String, reasons: MutableList<String>) {
    if (content.length !in CONTENT_MIN_LENGTH..CONTENT_MAX_LENGTH) {
        reasons.add("${CONTENT}:${ReviewValidation.ErrorMessages.CONTENT_SIZE}")
    }
}

private fun validateAuthorId(authorId: Int, reasons: MutableList<String>) {
    if (authorId < AUTHOR_ID_MIN_VALUE) {
        reasons.add("${AUTHOR_ID}:${ReviewValidation.ErrorMessages.AUTHOR_ID_POSITIVE}")
    }
}

private fun validateArticleId(articleId: Int, reasons: MutableList<String>) {
    if (articleId < ARTICLE_ID_MIN_VALUE) {
        reasons.add("${ARTICLE_ID}:${ReviewValidation.ErrorMessages.ARTICLE_ID_POSITIVE}")
    }
}

