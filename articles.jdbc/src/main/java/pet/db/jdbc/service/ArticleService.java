package pet.db.jdbc.service;

import jakarta.validation.constraints.NotNull;

import pet.db.jdbc.entity.Article;

import java.util.List;
import java.util.Optional;

public interface ArticleService {

    Article create(@NotNull Article article, @NotNull List<Integer> authorIds);

    Article updateById(@NotNull Article article, @NotNull Integer id);

    void deleteById(@NotNull Integer id);

    Optional<Article> findById(@NotNull Integer id);

    List<Article> findArticlesByAuthorId(@NotNull Integer authorId);

    List<Article> findByIds(@NotNull List<Integer> articleIds);

    List<Article> findAll();

    boolean existsById(@NotNull Integer id);

}
