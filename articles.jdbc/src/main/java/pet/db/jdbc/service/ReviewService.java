package pet.db.jdbc.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import pet.db.jdbc.controller.payload.NewArticlePayload;
import pet.db.jdbc.entity.Review;

import java.util.List;
import java.util.Optional;

public interface ReviewService {

    Review create(@NotNull Review review);

    void deleteById(@NotNull Integer id);

    Optional<Review> findById(@NotNull Integer id);

    List<Review> findByAuthorId(@NotNull Integer authorId);

    List<Review> findByArticleId(@NotNull Integer articleId);

}
