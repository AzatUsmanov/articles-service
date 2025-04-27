package pet.db.jdbc.model.dto.payload;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record NewArticlePayload (
        @NotNull(message = "topic must be not null")
        @Size(min = TOPIC_MIN_LENGTH, max = TOPIC_MAX_LENGTH,
                message = "the length of the topic must be between {min} and {max}")
        String topic,

        @NotNull(message = "content must be not null")
        @Size(min = CONTENT_MIN_LENGTH, max = CONTENT_MAX_LENGTH,
                message = "the length of the content must be between {min} and {max}")
        String content,
        @NotEmpty(message = "authorIds must be not empty")
        @NotNull(message = "username must be not null")
        List<Integer> authorIds) {

        public static final int TOPIC_MIN_LENGTH = 1;

        public static final int TOPIC_MAX_LENGTH = 50;

        public static final int CONTENT_MIN_LENGTH = 1;

        public static final int CONTENT_MAX_LENGTH = 1500;

}
