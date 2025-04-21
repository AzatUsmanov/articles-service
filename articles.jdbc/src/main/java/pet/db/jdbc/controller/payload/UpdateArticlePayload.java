package pet.db.jdbc.controller.payload;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateArticlePayload(
        @NotNull(message = "topic must be not null")
        @Size(min = NewArticlePayload.TOPIC_MIN_LENGTH, max = NewArticlePayload.TOPIC_MAX_LENGTH,
                message = "the length of the topic must be between " +
                        NewArticlePayload.TOPIC_MIN_LENGTH + " and " + NewArticlePayload.TOPIC_MAX_LENGTH)
        String topic,

        @NotNull(message = "content must be not null")
        @Size(min = NewArticlePayload.CONTENT_MIN_LENGTH, max = NewArticlePayload.CONTENT_MAX_LENGTH,
                message = "the length of the content must be between " +
                        NewArticlePayload.CONTENT_MIN_LENGTH + " and " + NewArticlePayload.CONTENT_MAX_LENGTH)
        String content) {
}
