package pet.db.jdbc.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import pet.db.jdbc.entity.Article;
import pet.db.jdbc.entity.Review;
import pet.db.jdbc.entity.User;
import pet.db.jdbc.tool.db.DbCleaner;
import pet.db.jdbc.tool.generator.TestDataGenerator;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class ReviewServiceTest {

    @Autowired
    private DbCleaner dbCleaner;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private TestDataGenerator<User> userTestDataGenerator;

    @Autowired
    private TestDataGenerator<Article> articleTestDataGenerator;

    @Autowired
    private TestDataGenerator<Review> reviewTestDataGenerator;


    @AfterEach
    void cleanDb() {
        dbCleaner.cleanAll();
    }

    @Test
    public void createReview() {
        Review reviewForSave = reviewTestDataGenerator.generateUnsavedData();

        Review savedReview = reviewService.create(reviewForSave);

        Optional<Review> reviewOptionalForCheck = reviewService.findById(savedReview.getId());
        assertTrue(reviewOptionalForCheck.isPresent());
        assertEquals(savedReview, reviewOptionalForCheck.get());
    }

    @Test
    public void createReviewWithInvalidData() {
        Review reviewForSave = reviewTestDataGenerator.generateUnsavedData();
        reviewForSave.setContent(null);

        assertThatThrownBy(() -> reviewService.create(reviewForSave))
                .isInstanceOf(RuntimeException.class);;
    }

    @Test
    public void createReviewWithNonExistentArticleId() {
        Review reviewForSave = reviewTestDataGenerator.generateUnsavedData();
        Article unsavedArticle = articleTestDataGenerator.generateUnsavedData();
        reviewForSave.setArticleId(unsavedArticle.getId());

        assertThatThrownBy(() -> reviewService.create(reviewForSave))
                .isInstanceOf(RuntimeException.class);;
    }

    @Test
    public void createReviewWithNonExistentAuthorId() {
        User unsavedAuthor = userTestDataGenerator.generateUnsavedData();
        Review reviewForSave = reviewTestDataGenerator.generateUnsavedData();
        reviewForSave.setAuthorId(unsavedAuthor.getId());

        assertThatThrownBy(() -> reviewService.create(reviewForSave))
                .isInstanceOf(RuntimeException.class);;
    }

    @Test
    public void deleteReviewById() {
        Review savedReview = reviewTestDataGenerator.generateSavedData();

        reviewService.deleteById(savedReview.getId());

        Optional<Review> reviewOptionalForCheck = reviewService.findById(savedReview.getId());
        assertTrue(reviewOptionalForCheck.isEmpty());
    }

    @Test
    public void deleteReviewByNonExistentId() {
        Review unsavedReview = reviewTestDataGenerator.generateUnsavedData();

        reviewService.deleteById(unsavedReview.getId());

        Optional<Review> reviewOptionalForCheck = reviewService.findById(unsavedReview.getId());
        assertTrue(reviewOptionalForCheck.isEmpty());
    }

    @Test
    public void findReviewById() {
        Review savedReview = reviewTestDataGenerator.generateSavedData();

        Optional<Review> reviewOptionalForCheck = reviewService.findById(savedReview.getId());

        assertTrue(reviewOptionalForCheck.isPresent());
        assertEquals(savedReview, reviewOptionalForCheck.get());
    }

    @Test
    public void findReviewByNonExistentId() {
        Review unsavedReview = reviewTestDataGenerator.generateUnsavedData();

        Optional<Review> reviewOptionalForCheck = reviewService.findById(unsavedReview.getId());

        assertTrue(reviewOptionalForCheck.isEmpty());
    }

    @Test
    public void findReviewsByAuthorId() {
        List<Review> reviews = reviewTestDataGenerator.generateSavedData(30);
        Integer authorId = reviews.getFirst().getAuthorId();
        List<Review> allReviewsByAuthor = reviews.stream()
                .filter(x -> Objects.equals(x.getAuthorId(), authorId))
                .toList();

        List<Review> reviewsForCheck = reviewService.findByAuthorId(authorId);


        assertEquals(allReviewsByAuthor.size(), reviewsForCheck.size());
        assertTrue(allReviewsByAuthor.containsAll(reviewsForCheck));
        assertTrue(reviewsForCheck.containsAll(allReviewsByAuthor));
    }

    @Test
    public void findReviewsByNonExistentAuthorId() {
        User unsavedAuthor = userTestDataGenerator.generateUnsavedData();

        assertThatThrownBy(() -> reviewService.findByAuthorId(unsavedAuthor.getId()))
                .isInstanceOf(NoSuchElementException.class);;
    }

    @Test
    public void findReviewsByArticleId() {
        List<Review> reviews = reviewTestDataGenerator.generateSavedData(30);
        Integer articleId = reviews.getFirst().getArticleId();
        List<Review> allReviewsForArticle = reviews.stream()
                .filter(x -> Objects.equals(x.getArticleId(), articleId))
                .toList();

        List<Review> reviewsForCheck = reviewService.findByArticleId(articleId);

        assertEquals(allReviewsForArticle.size(), reviewsForCheck.size());
        assertTrue(allReviewsForArticle.containsAll(reviewsForCheck));
        assertTrue(reviewsForCheck.containsAll(allReviewsForArticle));
    }

    @Test
    public void findReviewsByNonExistentArticleId() {
        Article unsavedArticle = articleTestDataGenerator.generateUnsavedData();

        assertThatThrownBy(() -> reviewService.findByArticleId(unsavedArticle.getId()))
                .isInstanceOf(NoSuchElementException.class);;
    }

}
