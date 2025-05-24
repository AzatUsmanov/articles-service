package pet.articles.config;

import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

import javax.sql.DataSource

class JooqConfig {

    fun dslContext(
        dataSource: DataSource,
        dialect: SQLDialect = SQLDialect.POSTGRES
    ): DSLContext = DSL.using(dataSource, dialect)
}
