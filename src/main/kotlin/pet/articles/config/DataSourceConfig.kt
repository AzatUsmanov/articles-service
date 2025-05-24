package pet.articles.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.koin.core.annotation.Module
import org.koin.core.annotation.Property
import org.koin.core.annotation.Single
import javax.sql.DataSource

class DataSourceConfig {

    fun dataSource(
        dataSourceUrl: String,
        dataSourceUsername: String,
        dataSourcePassword: String,
        dataSourceDriverClassName: String
    ): DataSource = HikariDataSource(
        HikariConfig().apply {
            jdbcUrl = dataSourceUrl
            username = dataSourceUsername
            password = dataSourcePassword
            driverClassName = dataSourceDriverClassName
        }
    )
}
