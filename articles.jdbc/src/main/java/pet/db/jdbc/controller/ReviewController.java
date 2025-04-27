package pet.db.jdbc.controller;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import pet.db.jdbc.model.dto.payload.ReviewPayload;
import pet.db.jdbc.model.dto.Review;
import pet.db.jdbc.service.ReviewService;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "${api.paths.reviews}",
        produces= MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE)
public class ReviewController {

    private final ReviewService reviewService;

    private final Converter<ReviewPayload, Review> reviewPayloadToReviewConverter;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Review create(@RequestBody @Valid ReviewPayload reviewPayload) {
        Review review = reviewPayloadToReviewConverter.convert(reviewPayload);
        return reviewService.create(review);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(@PathVariable("id") Integer id) {
        reviewService.deleteById(id);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Review> findById(@PathVariable("id") Integer id) {
        Optional<Review> reviewOptional = reviewService.findById(id);
        return reviewOptional
                .map(user -> new ResponseEntity<>(user, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/articles/{articleId}")
    public List<Review> findByArticleId(@PathVariable("articleId") Integer articleId) {
        return reviewService.findByArticleId(articleId);
    }

    @GetMapping("/users/{authorId}")
    public List<Review> findByAuthorId(@PathVariable("authorId") Integer authorId) {
        return reviewService.findByAuthorId(authorId);
    }

}
