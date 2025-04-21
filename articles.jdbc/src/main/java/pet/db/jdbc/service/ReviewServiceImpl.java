package pet.db.jdbc.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import pet.db.jdbc.entity.Review;
import pet.db.jdbc.repository.ReviewRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;

    private final UserService userService;

    private final ArticleService articleService;

    @Override
    public Review create(Review review) {
        return reviewRepository.save(review);
    }

    @Override
    public void deleteById(Integer id) {
        reviewRepository.deleteById(id);
    }

    @Override
    public Optional<Review> findById(Integer id) {
        return reviewRepository.findById(id);
    }

    @Override
    public List<Review> findByAuthorId(Integer authorId) {
        if (!userService.existsById(authorId)) {
            throw new NoSuchElementException("Attempt to find reviews by non existent author");
        }
        return reviewRepository.findByAuthorId(authorId);
    }

    @Override
    public List<Review> findByArticleId(Integer articleId) {
        if (!articleService.existsById(articleId)) {
            throw new NoSuchElementException("Attempt to find reviews by non existent article");
        }
        return reviewRepository.findByArticleId(articleId);
    }

}
