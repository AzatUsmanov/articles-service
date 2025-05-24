package pet.articles.config.di

import com.auth0.jwt.interfaces.JWTVerifier
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.output.MigrateResult
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.RecordMapper
import org.jooq.RecordUnmapper
import org.koin.core.KoinApplication
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.fileProperties
import pet.articles.config.DataSourceConfig
import pet.articles.config.FlywayConfig
import pet.articles.config.JWTVerifierConfig
import pet.articles.config.JooqConfig
import pet.articles.generated.jooq.tables.records.ArticlesRecord
import pet.articles.generated.jooq.tables.records.AuthorshipOfArticlesRecord
import pet.articles.generated.jooq.tables.records.ReviewsRecord
import pet.articles.generated.jooq.tables.records.UsersRecord
import pet.articles.model.dto.Article
import pet.articles.model.dto.AuthorshipOfArticle
import pet.articles.model.dto.Review
import pet.articles.model.dto.User
import pet.articles.repository.ArticleRepository
import pet.articles.repository.ArticleRepositoryImpl
import pet.articles.repository.AuthorshipOfArticleRepository
import pet.articles.repository.AuthorshipOfArticleRepositoryImpl
import pet.articles.repository.ReviewRepository
import pet.articles.repository.ReviewRepositoryImpl
import pet.articles.repository.UserRepository
import pet.articles.repository.UserRepositoryImpl
import pet.articles.service.ArticleService
import pet.articles.service.ArticleServiceImpl
import pet.articles.service.AuthService
import pet.articles.service.AuthServiceImpl
import pet.articles.service.RegistrationService
import pet.articles.service.RegistrationServiceImpl
import pet.articles.service.ReviewService
import pet.articles.service.ReviewServiceImpl
import pet.articles.service.UserService
import pet.articles.service.UserServiceImpl
import pet.articles.tool.extension.getProperty
import pet.articles.tool.jooq.mapper.ArticleRecordMapper
import pet.articles.tool.jooq.mapper.ReviewRecordMapper
import pet.articles.tool.jooq.mapper.UserRecordMapper
import pet.articles.tool.jooq.unmapper.ArticleRecordUnmapper
import pet.articles.tool.jooq.unmapper.AuthorshipRecordUnmapper
import pet.articles.tool.jooq.unmapper.ReviewRecordUnmapper
import pet.articles.tool.jooq.unmapper.UserRecordUnmapper
import javax.sql.DataSource

fun KoinApplication.configure() {
    printLogger()
    fileProperties()
    modules(
        configModule,
        toolModule,
        repositoryModule,
        serviceModule
    )
}

val configModule = module {
    single<DataSource> {
        DataSourceConfig()
            .dataSource(
                dataSourceUrl = getProperty("datasource.url"),
                dataSourceUsername = getProperty("datasource.username"),
                getProperty("datasource.password"),
                getProperty("datasource.driver-class-name")
            )
    }

    single<Flyway> {
        FlywayConfig().flyway(
            getProperty("flyway.locations"),
            get()
        )
    }
    single<MigrateResult>(createdAtStart = true) {
        FlywayConfig().migrate(
            get()
        )
    }
    single<DSLContext> {
        JooqConfig().dslContext(
            get()
        )
    }
    single<JWTVerifier> {
        JWTVerifierConfig().buildVerifier(
            secret = getProperty("jwt.secret"),
            issuer = getProperty("jwt.issuer"),
            audience = getProperty("jwt.audience")
        )
    }
}

val toolModule = module {
    single<RecordMapper<Record, User>>(named("UserRecordMapper")) {
        UserRecordMapper()
    }
    single<RecordMapper<Record, Article>>(named("ArticleRecordMapper")) {
        ArticleRecordMapper()
    }
    single<RecordMapper<Record, Review>>(named("ReviewRecordMapper")) {
        ReviewRecordMapper()
    }

    single<RecordUnmapper<User, UsersRecord>>(named("UserRecordUnmapper")) {
        UserRecordUnmapper()
    }
    single<RecordUnmapper<Article, ArticlesRecord>>(named("ArticleRecordUnmapper")) {
        ArticleRecordUnmapper()
    }
    single<RecordUnmapper<Review, ReviewsRecord>>(named("ReviewRecordUnmapper")) {
        ReviewRecordUnmapper()
    }
    single<RecordUnmapper<AuthorshipOfArticle, AuthorshipOfArticlesRecord>>(named("AuthorshipRecordUnmapper")) {
        AuthorshipRecordUnmapper()
    }
}

val repositoryModule = module {
    single<AuthorshipOfArticleRepository> {
        AuthorshipOfArticleRepositoryImpl(
            dsl = get()
        )
    }
    single<UserRepository> {
        UserRepositoryImpl(
            dsl = get(),
            userRecordMapper = get(named("UserRecordMapper")),
            userRecordUnmapper = get(named("UserRecordUnmapper"))
        )
    }
    single<ArticleRepository> {
        ArticleRepositoryImpl(
            dsl = get(),
            articleRecordMapper = get(named("ArticleRecordMapper")),
            articleRecordUnmapper = get(named("ArticleRecordUnmapper")),
            authorshipOfArticlesRecordUnmapper = get(named("AuthorshipRecordUnmapper"))
        )
    }
    single<ReviewRepository> {
        ReviewRepositoryImpl(
            dsl = get(),
            reviewRecordMapper = get(named("ReviewRecordMapper")),
            reviewRecordUnmapper = get(named("ReviewRecordUnmapper"))
        )
    }
}

val serviceModule = module {
    single<UserService> { UserServiceImpl(get(), get()) }
    single<ArticleService> { ArticleServiceImpl(get(), get()) }
    single<ReviewService> { ReviewServiceImpl(get()) }
    single<RegistrationService> { RegistrationServiceImpl(get(), getProperty("bcrypt.salt")) }
    single<AuthService> {
        AuthServiceImpl(
            userService = get(),
            secret = getProperty("jwt.secret"),
            audience = getProperty("jwt.audience"),
            issuer = getProperty("jwt.issuer"),
            expiresIn = getProperty("jwt.expiresIn")
        )
    }
}
