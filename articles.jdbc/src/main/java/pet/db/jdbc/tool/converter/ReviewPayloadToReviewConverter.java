package pet.db.jdbc.tool.converter;


import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import pet.db.jdbc.model.dto.Review;
import pet.db.jdbc.model.dto.payload.ReviewPayload;

import java.time.LocalDateTime;

@Component
public class ReviewPayloadToReviewConverter implements Converter<ReviewPayload, Review> {

        @Override
        public Review convert(ReviewPayload source) {
                return Review.builder()
                        .type(source.type())
                        .content(source.content())
                        .authorId(source.authorId())
                        .articleId(source.articleId())
                        .dateOfCreation(LocalDateTime.now())
                        .build();
        }

}
