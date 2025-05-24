package pet.articles.tool.db


import org.koin.core.annotation.Single
import java.sql.PreparedStatement
import javax.sql.DataSource

class DBCleanerImpl(
    private val dataSource: DataSource
) : DBCleaner {

    companion object {
        private const val DELETE_ALL = "DELETE FROM %s"

        private const val REVIEWS_TABLE_NAME = "public.reviews"
        private const val AUTHORSHIP_TABLE_NAME = "public.authorship_of_articles"
        private const val ARTICLES_TABLE_NAME = "public.articles"
        private const val USERS_TABLE_NAME = "public.users"
    }

    override fun cleanUp() {
        cleanTable(REVIEWS_TABLE_NAME)
        cleanTable(AUTHORSHIP_TABLE_NAME)
        cleanTable(ARTICLES_TABLE_NAME)
        cleanTable(USERS_TABLE_NAME)
    }

    private fun cleanTable(tableName: String) {
        dataSource.connection.use { connection ->
            connection
                .prepareStatement(DELETE_ALL.format(tableName))
                .use(PreparedStatement::executeUpdate)
        }
    }
}