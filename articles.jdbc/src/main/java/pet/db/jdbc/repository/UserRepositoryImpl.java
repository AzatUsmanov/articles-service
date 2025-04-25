package pet.db.jdbc.repository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Repository;

import pet.db.jdbc.entity.User;
import pet.db.jdbc.util.SqlUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private static final String SAVE_USER = "INSERT INTO users(id, username, email, password, role) values(DEFAULT, ?, ?, ?, ?)";

    private static final String UPDATE_USER_BY_ID = "UPDATE users SET username = ?, email= ?, password = ?, role = ? WHERE id = ?";

    private static final String DELETE_USER_BY_ID = "DELETE FROM users WHERE id = ?";

    private static final String FIND_USER_BY_ID = "SELECT * FROM users WHERE id = ?";

    private static final String FIND_USER_BY_EMAIL = "SELECT * FROM users WHERE email = ?";

    private static final String FIND_USER_BY_USERNAME = "SELECT * FROM users WHERE username = ?";

    private static final String FIND_ALL_USERS = "SELECT * FROM users";

    private static final String FIND_USERS_BY_IDS = "SELECT * FROM users WHERE id in (%s)";

    private final DataSource dataSource;

    @Override
    public User save(User user) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement prepareStatement = connection.prepareStatement(SAVE_USER)) {
            prepareStatement.setString(1, user.getUsername());
            prepareStatement.setString(2, user.getEmail());
            prepareStatement.setString(3, user.getPassword());
            prepareStatement.setInt(4, user.getRole().getNumber());
            prepareStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return findByUsername(user.getUsername())
                .orElseThrow(NoSuchElementException::new);
    }

    @Override
    public User updateById(User user, Integer id) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement prepareStatement = connection.prepareStatement(UPDATE_USER_BY_ID)) {
            prepareStatement.setString(1, user.getUsername());
            prepareStatement.setString(2, user.getEmail());
            prepareStatement.setString(3, user.getPassword());
            prepareStatement.setInt(4, user.getRole().getNumber());
            prepareStatement.setInt(5, id);
            prepareStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return findById(id)
                .orElseThrow(NoSuchElementException::new);
    }

    @Override
    public void deleteById(Integer id) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement prepareStatement = connection.prepareStatement(DELETE_USER_BY_ID)) {
            prepareStatement.setInt(1, id);
            prepareStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean existsById(Integer id) {
        return findById(id).isPresent();
    }

    @Override
    public Optional<User> findById(Integer id) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement prepareStatement = connection.prepareStatement(FIND_USER_BY_ID)) {
            prepareStatement.setInt(1, id);

            try (ResultSet resultSet = prepareStatement.executeQuery()) {
                return resultSet.next() ? Optional.of(getUserFromResultSet(resultSet)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean existsByUsername(String username) {
        return findByUsername(username).isPresent();
    }

    @Override
    public Optional<User> findByUsername(String username) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement prepareStatement = connection.prepareStatement(FIND_USER_BY_USERNAME)) {
            prepareStatement.setString(1, username);

            try (ResultSet resultSet = prepareStatement.executeQuery()) {
                return resultSet.next() ? Optional.of(getUserFromResultSet(resultSet)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean existsByEmail(String email) {
        return findByEmail(email).isPresent();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement prepareStatement = connection.prepareStatement(FIND_USER_BY_EMAIL)) {
            prepareStatement.setString(1, email);

            try (ResultSet resultSet = prepareStatement.executeQuery()) {
                return resultSet.next() ? Optional.of(getUserFromResultSet(resultSet)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<User> findAll() {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement prepareStatement = connection.prepareStatement(FIND_ALL_USERS);
             ResultSet resultSet = prepareStatement.executeQuery()) {
            List<User> users = new ArrayList<>();
            while (resultSet.next()) {
                users.add(getUserFromResultSet(resultSet));
            }
            return users;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<User> findByIds(List<Integer> ids) {
        String sqlRequest = SqlUtils.buildInClause(FIND_USERS_BY_IDS, ids);

        try (Connection connection = dataSource.getConnection();
             PreparedStatement prepareStatement = connection.prepareStatement(sqlRequest)) {
            try (ResultSet resultSet = prepareStatement.executeQuery()) {
                List<User> users = new ArrayList<>();
                while (resultSet.next()) {
                    users.add(getUserFromResultSet(resultSet));
                }
                return users;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private User getUserFromResultSet(ResultSet resultSet) throws SQLException {
        return User.builder()
                .id(resultSet.getInt(User.Column.ID.toString()))
                .username(resultSet.getString(User.Column.USERNAME.toString()))
                .email(resultSet.getString(User.Column.EMAIL.toString()))
                .password(resultSet.getString(User.Column.PASSWORD.toString()))
                .role(User.Role.getByNumber(
                        resultSet.getInt(User.Column.ROLE.toString())))
                .build();
    }

}
