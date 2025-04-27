package pet.db.jdbc.repository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Repository;

import pet.db.jdbc.model.dto.Article;
import pet.db.jdbc.model.enums.ArticleColumn;
import pet.db.jdbc.util.SqlUtils;

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
public class ArticleRepositoryImpl implements ArticleRepository {

    private static final String SAVE_ARTICLE = "INSERT INTO articles(id, date_of_creation, topic, content) values(DEFAULT, ?, ?, ?)";

    private static final String UPDATE_ARTICLE_BY_ID = "UPDATE articles SET topic = ?, content = ? WHERE id = ?";

    private static final String DELETE_ARTICLE_BY_ID = "DELETE FROM articles WHERE id = ?";

    private static final String FIND_ARTICLE_BY_ID = "SELECT * FROM articles WHERE id = ?";

    private static final String FIND_ALL_ARTICLES = "SELECT * FROM articles";

    private static final String FIND_ARTICLES_BY_IDS = "SELECT * FROM articles WHERE id in (%s)";

    private final DataSource dataSource;

    @Override
    public Article save(Article article) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement prepareStatement = connection.prepareStatement(SAVE_ARTICLE, PreparedStatement.RETURN_GENERATED_KEYS)) {
            prepareStatement.setTimestamp(1, Timestamp.valueOf(article.getDateOfCreation()));
            prepareStatement.setString(2, article.getTopic());
            prepareStatement.setString(3, article.getContent());
            prepareStatement.executeUpdate();

            try (ResultSet generatedKeys = prepareStatement.getGeneratedKeys()) {
                if (!generatedKeys.next()) {
                    throw new RuntimeException();
                }
                article.setId(generatedKeys.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return findById(article.getId())
                .orElseThrow(NoSuchElementException::new);
    }

    @Override
    public Article updateById(Article article, Integer id) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement prepareStatement = connection.prepareStatement(UPDATE_ARTICLE_BY_ID)) {
            prepareStatement.setString(1, article.getTopic());
            prepareStatement.setString(2, article.getContent());
            prepareStatement.setInt(3, id);
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
             PreparedStatement prepareStatement = connection.prepareStatement(DELETE_ARTICLE_BY_ID)) {
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
    public Optional<Article> findById(Integer id) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement prepareStatement = connection.prepareStatement(FIND_ARTICLE_BY_ID)) {
            prepareStatement.setInt(1, id);

            try (ResultSet resultSet = prepareStatement.executeQuery()) {
                return resultSet.next() ? Optional.of(getArticleFromResultSet(resultSet)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Article> findAll() {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement prepareStatement = connection.prepareStatement(FIND_ALL_ARTICLES);
             ResultSet resultSet = prepareStatement.executeQuery()) {
            List<Article> articles = new ArrayList<>();
            while (resultSet.next()) {
                articles.add(getArticleFromResultSet(resultSet));
            }
            return articles;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Article> findByIds(List<Integer> ids) {
        String sqlRequest = SqlUtils.buildInClause(FIND_ARTICLES_BY_IDS, ids);

        try (Connection connection = dataSource.getConnection();
             PreparedStatement prepareStatement = connection.prepareStatement(sqlRequest)) {

            try (ResultSet resultSet = prepareStatement.executeQuery()) {
                List<Article> articles = new ArrayList<>();
                while (resultSet.next()) {
                    articles.add(getArticleFromResultSet(resultSet));
                }
                return articles;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Article getArticleFromResultSet(ResultSet resultSet) throws SQLException {
        return Article.builder()
                .id(resultSet.getInt(ArticleColumn.ID.toString()))
                .topic(resultSet.getString(ArticleColumn.TOPIC.toString()))
                .content(resultSet.getString(ArticleColumn.CONTENT.toString()))
                .dateOfCreation(
                        resultSet.getTimestamp(ArticleColumn.DATE_OF_CREATION.toString()).toLocalDateTime())
                .build();
    }

}
