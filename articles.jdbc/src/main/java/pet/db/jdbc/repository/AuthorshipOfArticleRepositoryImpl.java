package pet.db.jdbc.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import pet.db.jdbc.entity.AuthorshipOfArticle;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class AuthorshipOfArticleRepositoryImpl implements AuthorshipOfArticleRepository {

    private static final String SAVE_AUTHORSHIP_OF_ARTICLES = "INSERT INTO authorship_of_articles(author_id, article_id) values(?, ?)";

    private static final String FIND_AUTHOR_IDS_BY_ARTICLE_ID = "SELECT * FROM authorship_of_articles WHERE article_id = ?";

    private static final String FIND_ARTICLE_IDS_BY_AUTHOR_ID = "SELECT * FROM authorship_of_articles WHERE author_id = ?";

    private static final String FIND_AUTHORSHIP_OF_ARTICLES_BY_AUTHOR_ID_AND_ARTICLE_ID = "SELECT * FROM authorship_of_articles WHERE author_id = ? AND article_id = ?";

    private final DataSource dataSource;

    @Override
    public List<AuthorshipOfArticle> save(List<AuthorshipOfArticle> authorshipOfArticles) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement prepareStatement = connection.prepareStatement(SAVE_AUTHORSHIP_OF_ARTICLES)) {
            connection.setAutoCommit(false);

            for (AuthorshipOfArticle authorshipOfArticle : authorshipOfArticles) {
                prepareStatement.setInt(1, authorshipOfArticle.getAuthorId());
                prepareStatement.setInt(2, authorshipOfArticle.getArticleId());
                prepareStatement.addBatch();
            }
            prepareStatement.executeBatch();
            connection.commit();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return  authorshipOfArticles;
    }

    @Override
    public List<Integer> findAuthorIdsByArticleId(Integer articleId) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement prepareStatement = connection.prepareStatement(FIND_AUTHOR_IDS_BY_ARTICLE_ID)) {
            prepareStatement.setInt(1, articleId);

            try (ResultSet resultSet = prepareStatement.executeQuery()) {
                List<Integer> authorIds = new ArrayList<>();
                while (resultSet.next()) {
                    authorIds.add(resultSet.getInt("author_id"));
                }
                return authorIds;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Integer> findArticleIdsByAuthorId(Integer authorId) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement prepareStatement = connection.prepareStatement(FIND_ARTICLE_IDS_BY_AUTHOR_ID)) {
            prepareStatement.setInt(1, authorId);

            try (ResultSet resultSet = prepareStatement.executeQuery()) {
                List<Integer> articleIds = new ArrayList<>();
                while (resultSet.next()) {
                    articleIds.add(resultSet.getInt("article_id"));
                }
                return articleIds;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean exists(AuthorshipOfArticle authorshipOfArticle) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement prepareStatement = connection.prepareStatement(FIND_AUTHORSHIP_OF_ARTICLES_BY_AUTHOR_ID_AND_ARTICLE_ID)) {
            prepareStatement.setInt(1, authorshipOfArticle.getAuthorId());
            prepareStatement.setInt(2, authorshipOfArticle.getArticleId());

            try (ResultSet resultSet = prepareStatement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
