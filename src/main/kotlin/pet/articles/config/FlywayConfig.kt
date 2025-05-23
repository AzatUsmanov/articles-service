package pet.articles.config

import org.flywaydb.core.Flyway
import org.flywaydb.core.api.output.MigrateResult
import org.koin.core.annotation.Module
import org.koin.core.annotation.Property
import org.koin.core.annotation.Single

import javax.sql.DataSource

@Module
class FlywayConfig {

    @Single
    fun flyway(
        @Property("flyway.locations") flywayLocations: String,
        dataSource: DataSource
    ): Flyway = Flyway.configure()
        .locations(flywayLocations)
		.dataSource(dataSource)
        .load()

    @Single(createdAtStart = true)
    fun migrate(flyway: Flyway): MigrateResult = flyway.migrate()
}
