package pet.db.jdbc.repository;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import pet.db.jdbc.entity.User;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface UserRepository {

    User save(@NotNull User user);

    User updateById(@NotNull User user, @NotNull Integer id);

    void deleteById(@NotNull Integer id);

    Optional<User> findById(@NotNull Integer id);

    Optional<User> findByUsername(@NotNull String username);

    Optional<User> findByEmail(@NotNull String email);

    List<User> findByIds(@NotNull @NotEmpty List<Integer> ids);

    List<User> findAll();

    boolean existsById(@NotNull Integer id);

    boolean existsByUsername(@NotNull String username);

    boolean existsByEmail(@NotNull String email);

}
