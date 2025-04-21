package pet.db.jdbc.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import pet.db.jdbc.controller.payload.NewArticlePayload;
import pet.db.jdbc.controller.payload.UpdateArticlePayload;
import pet.db.jdbc.entity.Article;

import java.util.List;
import java.util.Optional;

public interface ArticleService {

    Article create(@NotNull Article article, @NotNull List<Integer> authorIds);

    Article updateById(@NotNull Article article, @NotNull Integer id);

    void deleteById(@NotNull Integer id);

    Optional<Article> findById(@NotNull Integer id);

    List<Article> findArticlesByAuthorId(@NotNull Integer authorId);

    List<Article> findByIds(List<Integer> articleIds);

    List<Article> findAll();

    boolean existsById(@NotNull Integer id);

}
