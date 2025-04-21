package pet.db.jdbc.repository;

import jakarta.validation.constraints.NotNull;

import pet.db.jdbc.entity.AuthorshipOfArticle;

import java.util.List;

public interface AuthorshipOfArticleRepository {

    List<AuthorshipOfArticle> save(@NotNull List<AuthorshipOfArticle> authorshipOfArticles);

    List<Integer> findAuthorIdsByArticleId(@NotNull Integer articleId);

    List<Integer> findArticleIdsByAuthorId(@NotNull Integer authorId);

    boolean exists(@NotNull AuthorshipOfArticle authorshipOfArticle);

}

