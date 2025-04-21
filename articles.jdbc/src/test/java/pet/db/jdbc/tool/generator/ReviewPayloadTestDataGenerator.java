package pet.db.jdbc.tool.generator;

import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.units.qual.A;
import org.instancio.Instancio;
import org.instancio.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pet.db.jdbc.controller.payload.ReviewPayload;
import pet.db.jdbc.controller.payload.UserPayload;
import pet.db.jdbc.entity.Article;
import pet.db.jdbc.entity.Review;
import pet.db.jdbc.entity.User;
import pet.db.jdbc.service.ReviewService;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ReviewPayloadTestDataGenerator implements TestDataGenerator<ReviewPayload> {

    @Autowired
    private TestDataGenerator<Review> reviewTestDataGenerator;

    @Override
    public ReviewPayload generateSavedData() {
        return generateSavedData(1).getFirst();
    }

    @Override
    public ReviewPayload generateUnsavedData() {
        return generateUnsavedData(1).getFirst();
    }

    @Override
    public List<ReviewPayload> generateSavedData(Integer dataSize) {
        return reviewTestDataGenerator.generateSavedData(dataSize).stream()
                .map(this::convertToReviewPayload)
                .toList();
    }

    @Override
    public List<ReviewPayload> generateUnsavedData(Integer dataSize) {
        return reviewTestDataGenerator.generateUnsavedData(dataSize).stream()
                .map(this::convertToReviewPayload)
                .toList();
    }

    private ReviewPayload convertToReviewPayload(Review review) {
        return new ReviewPayload(
                review.getType(),
                review.getContent(),
                review.getAuthorId(),
                review.getArticleId());
    }

}
