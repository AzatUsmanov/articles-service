package pet.db.jdbc.repository;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import pet.db.jdbc.model.dto.Article;

import java.util.List;
import java.util.Optional;

public interface ArticleRepository {

    Article save(@NotNull Article article);

    Article updateById(@NotNull Article article, Integer id);

    void deleteById(@NotNull Integer id);

    Optional<Article> findById(@NotNull Integer id);

    List<Article> findByIds(@NotNull @NotEmpty List<Integer> ids);

    List<Article> findAll();

    boolean existsById(@NotNull Integer id);

}
