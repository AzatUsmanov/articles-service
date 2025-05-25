package pet.articles.config.di

import com.auth0.jwt.interfaces.JWTVerifier
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.output.MigrateResult
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.RecordMapper
import org.jooq.RecordUnmapper
import org.koin.core.KoinApplication
import org.koin.core.logger.Level
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.fileProperties
import org.koin.logger.SLF4JLogger
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
import pet.articles.repository.article.ArticleRepository
import pet.articles.repository.article.ArticleRepositoryImpl
import pet.articles.repository.authorship.AuthorshipOfArticleRepository
import pet.articles.repository.authorship.AuthorshipOfArticleRepositoryImpl
import pet.articles.repository.review.ReviewRepository
import pet.articles.repository.review.ReviewRepositoryImpl
import pet.articles.repository.user.UserRepository
import pet.articles.repository.user.UserRepositoryImpl
import pet.articles.service.article.ArticleService
import pet.articles.service.article.ArticleServiceImpl
import pet.articles.service.user.AuthService
import pet.articles.service.user.AuthServiceImpl
import pet.articles.service.user.RegistrationService
import pet.articles.service.user.RegistrationServiceImpl
import pet.articles.service.review.ReviewService
import pet.articles.service.review.ReviewServiceImpl
import pet.articles.service.user.UserExistenceChecker
import pet.articles.service.user.UserExistenceCheckerImpl
import pet.articles.service.user.UserService
import pet.articles.service.user.UserServiceImpl
import pet.articles.tool.jooq.mapper.ArticleRecordMapper
import pet.articles.tool.jooq.mapper.ReviewRecordMapper
import pet.articles.tool.jooq.mapper.UserRecordMapper
import pet.articles.tool.jooq.unmapper.ArticleRecordUnmapper
import pet.articles.tool.jooq.unmapper.AuthorshipRecordUnmapper
import pet.articles.tool.jooq.unmapper.ReviewRecordUnmapper
import pet.articles.tool.jooq.unmapper.UserRecordUnmapper
import pet.articles.tool.validator.UniquenessValidator
import pet.articles.tool.validator.UserUniquenessValidator
import javax.sql.DataSource

fun KoinApplication.configure() {
    logger(SLF4JLogger(level = Level.INFO))
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
        JWTVerifierConfig().verifier(
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

    single<UniquenessValidator<User>>(named("UserUniquenessValidator")) {
        UserUniquenessValidator(get())
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
    single<UserExistenceChecker> { UserExistenceCheckerImpl(get()) }
    single<UserService> { UserServiceImpl(get(), get(), get(), get(named("UserUniquenessValidator")),logger) }
    single<ArticleService> { ArticleServiceImpl(get(), get(), logger) }
    single<ReviewService> { ReviewServiceImpl(get(), logger) }
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
