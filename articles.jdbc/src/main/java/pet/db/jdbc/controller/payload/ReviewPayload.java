package pet.db.jdbc.controller.payload;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import pet.db.jdbc.entity.Review;

public record ReviewPayload(
        @NotNull(message = "review type must not be null")
        Review.Type type,

        @NotNull(message = "content must be not null")
        @Size(min = CONTENT_MIN_LENGTH, max = CONTENT_MAX_LENGTH,
                message = "the length of the content must be between {min} and {max}")
        String content,

        @Positive(message = "authorId type must be positive")
        @NotNull(message = "authorId type must not be null")
        Integer authorId,

        @Positive(message = "articleId type must be positive")
        @NotNull(message = "articleId type must not be null")
        Integer articleId) {

        public static final int CONTENT_MIN_LENGTH = 1;

        public static final int CONTENT_MAX_LENGTH = 500;

}
