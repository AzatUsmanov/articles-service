package pet.articles.config

import org.flywaydb.core.Flyway
import org.flywaydb.core.api.output.MigrateResult
import org.koin.core.annotation.Module
import org.koin.core.annotation.Property
import org.koin.core.annotation.Single

import javax.sql.DataSource

class FlywayConfig {

    fun flyway(
        flywayLocations: String,
        dataSource: DataSource
    ): Flyway = Flyway.configure()
        .locations(flywayLocations)
        .dataSource(dataSource)
        .load()

    fun migrate(flyway: Flyway): MigrateResult =
        flyway.migrate()
}
