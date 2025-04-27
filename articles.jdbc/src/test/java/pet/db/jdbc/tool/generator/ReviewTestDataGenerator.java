package pet.db.jdbc.tool.generator;

import lombok.RequiredArgsConstructor;

import org.instancio.Instancio;
import org.instancio.Select;

import org.springframework.stereotype.Component;

import pet.db.jdbc.model.dto.payload.ReviewPayload;
import pet.db.jdbc.model.dto.Article;
import pet.db.jdbc.model.dto.Review;
import pet.db.jdbc.model.dto.User;
import pet.db.jdbc.service.ReviewService;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ReviewTestDataGenerator implements TestDataGenerator<Review> {

    private final ReviewService reviewService;

    private final TestDataGenerator<User> userTestDataGenerator;

    private final TestDataGenerator<Article> articleTestDataGenerator;

    @Override
    public Review generateSavedData() {
        return generateSavedData(1).getFirst();
    }

    @Override
    public Review generateUnsavedData() {
        return generateUnsavedData(1).getFirst();
    }

    @Override
    public List<Review> generateSavedData(Integer dataSize) {
        return generateUnsavedData(dataSize).stream()
                .map(reviewService::create)
                .toList();
    }

    @Override
    public List<Review> generateUnsavedData(Integer dataSize) {
        return Instancio
                .ofList(Review.class)
                .size(dataSize)
                .set(Select.field(Review::getDateOfCreation), LocalDateTime.now())
                .set(Select.field(Review::getAuthorId), userTestDataGenerator.generateSavedData().getId())
                .set(Select.field(Review::getArticleId), articleTestDataGenerator.generateSavedData().getId())
                .generate(Select.field(Review::getContent), gen -> gen.string()
                        .length(ReviewPayload.CONTENT_MIN_LENGTH, ReviewPayload.CONTENT_MAX_LENGTH)
                        .alphaNumeric())
                .create();
    }

}
