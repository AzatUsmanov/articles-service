package pet.db.jdbc.tool.db;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Component
@RequiredArgsConstructor
public class DbCleanerImpl implements DbCleaner {

    private static final String DELETE_ALL = "DELETE FROM %s";

    private final DataSource dataSource;

    @Override
    public void cleanAll() {
        try {
            cleanTable("reviews");
            cleanTable("authorship_of_articles");
            cleanTable("articles");
            cleanTable("users");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }}

    private void cleanTable(String tableName) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement prepareStatement = connection.prepareStatement(
                     String.format(DELETE_ALL, tableName))) {
            prepareStatement.executeUpdate();
        }
    }

}
