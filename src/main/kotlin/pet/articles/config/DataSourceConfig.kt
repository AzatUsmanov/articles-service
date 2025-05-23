package pet.articles.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.koin.core.annotation.Module
import org.koin.core.annotation.Property
import org.koin.core.annotation.Single
import javax.sql.DataSource

@Module
class DataSourceConfig {

    @Single
    fun dataSource(
        @Property("datasource.url") dataSourceUrl: String,
        @Property("datasource.username") dataSourceUsername: String,
        @Property("datasource.password") dataSourcePassword: String,
        @Property("datasource.driver-class-name") dataSourceDriverClassName: String
    ): DataSource =
        HikariDataSource(
            HikariConfig().apply {
                jdbcUrl = dataSourceUrl
                username = dataSourceUsername
                password = dataSourcePassword
                driverClassName = dataSourceDriverClassName
            }
        )
}
