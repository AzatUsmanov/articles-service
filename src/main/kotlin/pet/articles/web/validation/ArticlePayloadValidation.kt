package pet.articles.web.validation

import pet.articles.web.validation.ArticlePayloadValidation.ErrorMessages.CONTENT_SIZE
import pet.articles.web.validation.ArticlePayloadValidation.ErrorMessages.TOPIC_SIZE
import pet.articles.web.validation.ArticlePayloadValidation.Fields.Constraints.CONTENT_MAX_LENGTH
import pet.articles.web.validation.ArticlePayloadValidation.Fields.Constraints.CONTENT_MIN_LENGTH
import pet.articles.web.validation.ArticlePayloadValidation.Fields.Constraints.TOPIC_MAX_LENGTH
import pet.articles.web.validation.ArticlePayloadValidation.Fields.Constraints.TOPIC_MIN_LENGTH
import pet.articles.web.validation.ArticlePayloadValidation.Fields.DefaultValues.EMPTY_AUTHOR_IDS
import pet.articles.web.validation.ArticlePayloadValidation.Fields.Names.CONTENT
import pet.articles.web.validation.ArticlePayloadValidation.Fields.Names.TOPIC
import io.ktor.server.plugins.requestvalidation.*
import pet.articles.model.dto.payload.NewArticlePayload
import pet.articles.model.dto.payload.UpdateArticlePayload

object ArticlePayloadValidation {

    object Fields {

        object Names {
            const val TOPIC = "topic"
            const val CONTENT = "content"
        }

        object Constraints {
            const val TOPIC_MIN_LENGTH = 1
            const val TOPIC_MAX_LENGTH = 50
            const val CONTENT_MIN_LENGTH = 1
            const val CONTENT_MAX_LENGTH = 150
        }

        object DefaultValues {
            val EMPTY_AUTHOR_IDS = emptyList<Int>()
        }
    }

    object ErrorMessages {
        const val TOPIC_SIZE = "the length of the topic must be between $TOPIC_MIN_LENGTH and $TOPIC_MAX_LENGTH"
        const val CONTENT_SIZE = "the length of the content must be between $CONTENT_MIN_LENGTH and $CONTENT_MAX_LENGTH"
    }
}

fun NewArticlePayload.validate(): ValidationResult {
    val reasons: MutableList<String> = ArrayList()

    validateTopic(topic, reasons)
    validateContent(content, reasons)

    return if (reasons.isEmpty()) {
        ValidationResult.Valid
    } else ValidationResult.Invalid(reasons)
}

fun UpdateArticlePayload.validate(): ValidationResult =
    NewArticlePayload(topic, content, EMPTY_AUTHOR_IDS).validate()

private fun validateTopic(topic: String, reasons: MutableList<String>) {
    if (topic.length !in TOPIC_MIN_LENGTH..TOPIC_MAX_LENGTH) {
        reasons.add("$TOPIC:$TOPIC_SIZE")
    }
}

private fun validateContent(content: String, reasons: MutableList<String>) {
    if (content.length !in CONTENT_MIN_LENGTH..CONTENT_MAX_LENGTH) {
        reasons.add("$CONTENT:$CONTENT_SIZE")
    }
}