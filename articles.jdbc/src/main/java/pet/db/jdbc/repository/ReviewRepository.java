package pet.db.jdbc.repository;

import jakarta.validation.constraints.NotNull;
import pet.db.jdbc.entity.Review;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface ReviewRepository {

    Review save(@NotNull Review review);

    void deleteById(@NotNull Integer id);

    Optional<Review> findById(@NotNull Integer id);

    List<Review> findByAuthorId(@NotNull Integer authorId);

    List<Review> findByArticleId(@NotNull Integer articleId);

}
