package pet.db.jdbc.service;

import jakarta.validation.constraints.NotNull;
import pet.db.jdbc.entity.User;
import pet.db.jdbc.tool.exception.DuplicateUserException;

import java.util.List;
import java.util.Optional;

public interface UserService {

    User create(@NotNull User user) throws DuplicateUserException;

    User updateById(@NotNull User user, @NotNull Integer id) throws DuplicateUserException;

    void deleteById(@NotNull Integer id);

    Optional<User> findById(@NotNull Integer id);

    Optional<User> findByUsername(@NotNull String username);

    List<User> findByIds(@NotNull List<Integer> userIds);

    List<User> findAll();

    List<User> findAuthorsByArticleId(@NotNull Integer articleId);

    List<Integer> findAuthorIdsByArticleId(@NotNull Integer articleId);

    boolean existsById(@NotNull Integer id);

    boolean existsByUsername(@NotNull String username);

    boolean existsByEmail(@NotNull String email);

}
