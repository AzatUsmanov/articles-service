package pet.db.jdbc.repository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Repository;

import pet.db.jdbc.entity.Review;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ReviewRepositoryImpl implements ReviewRepository {

    private static final String SAVE_REVIEW = "INSERT INTO reviews(id, type, date_of_creation, content, author_id, article_id) values(DEFAULT, ?, ?, ?, ?, ?)";

    private static final String DELETE_REVIEW_BY_ID = "DELETE FROM reviews WHERE id = ?";

    private static final String FIND_REVIEW_BY_ID = "SELECT * FROM reviews WHERE id = ?";

    private static final String FIND_REVIEWS_BY_AUTHOR_ID = "SELECT * FROM reviews WHERE author_id = ?";

    private static final String FIND_REVIEWS_BY_ARTICLE_ID = "SELECT * FROM reviews WHERE article_id = ?";

    private final DataSource dataSource;

    @Override
    public Review save(Review review) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement prepareStatement = connection.prepareStatement(SAVE_REVIEW, PreparedStatement.RETURN_GENERATED_KEYS)) {
            prepareStatement.setInt(1, review.getType().getNumber());
            prepareStatement.setTimestamp(2, Timestamp.valueOf(review.getDateOfCreation()));
            prepareStatement.setString(3, review.getContent());
            prepareStatement.setInt(4, review.getAuthorId());
            prepareStatement.setInt(5, review.getArticleId());
            prepareStatement.executeUpdate();

            try (ResultSet generatedKeys = prepareStatement.getGeneratedKeys()) {
                if (!generatedKeys.next()) {
                    throw new RuntimeException();
                }
                review.setId(generatedKeys.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return findById(review.getId())
                .orElseThrow(NoSuchElementException::new);
    }

    @Override
    public void deleteById(Integer id) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement prepareStatement = connection.prepareStatement(DELETE_REVIEW_BY_ID)) {
            prepareStatement.setInt(1, id);
            prepareStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Review> findById(Integer id) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement prepareStatement = connection.prepareStatement(FIND_REVIEW_BY_ID)) {
            prepareStatement.setInt(1, id);

            try (ResultSet resultSet = prepareStatement.executeQuery()) {
                return resultSet.next() ? Optional.of(getReviewFromResultSet(resultSet)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Review> findByAuthorId(Integer authorId) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement prepareStatement = connection.prepareStatement(FIND_REVIEWS_BY_AUTHOR_ID)) {
            prepareStatement.setInt(1, authorId);

            try (ResultSet resultSet = prepareStatement.executeQuery()) {
                List<Review> reviews = new ArrayList<>();
                while (resultSet.next()) {
                    reviews.add(getReviewFromResultSet(resultSet));
                }
                return reviews;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Review> findByArticleId(Integer articleId) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement prepareStatement = connection.prepareStatement(FIND_REVIEWS_BY_ARTICLE_ID)) {
            prepareStatement.setInt(1, articleId);

            try (ResultSet resultSet = prepareStatement.executeQuery()) {
                List<Review> reviews = new ArrayList<>();
                while (resultSet.next()) {
                    reviews.add(getReviewFromResultSet(resultSet));
                }
                return reviews;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Review getReviewFromResultSet(ResultSet resultSet) throws SQLException {
        return Review.builder()
                .id(resultSet.getInt(Review.Column.ID.toString()))
                .type(Review.Type.getByNumber(
                        resultSet.getInt(Review.Column.TYPE.toString())))
                .dateOfCreation(
                        resultSet.getTimestamp(Review.Column.DATE_OF_CREATION.toString()).toLocalDateTime())
                .content(resultSet.getString(Review.Column.CONTENT.toString()))
                .authorId(resultSet.getInt(Review.Column.AUTHOR_ID.toString()))
                .articleId(resultSet.getInt(Review.Column.ARTICLE_ID.toString()))
                .build();
    }

}
