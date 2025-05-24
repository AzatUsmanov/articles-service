package pet.articles.config

import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.koin.core.KoinApplication
import org.koin.core.qualifier.named
import org.koin.dsl.module
import pet.articles.config.JooqConfig
import pet.articles.config.di.configure
import pet.articles.model.dto.Article
import pet.articles.model.dto.Review
import pet.articles.model.dto.User
import pet.articles.model.dto.payload.NewArticlePayload
import pet.articles.model.dto.payload.RegistrationPayload
import pet.articles.model.dto.payload.ReviewPayload
import pet.articles.model.dto.payload.UpdateArticlePayload
import pet.articles.model.dto.payload.UserPayload
import pet.articles.tool.db.DBCleaner
import pet.articles.tool.db.DBCleanerImpl
import pet.articles.tool.generator.ArticleTestDataGenerator
import pet.articles.tool.generator.NewArticlePayloadTestDataGenerator
import pet.articles.tool.generator.RegistrationPayloadTestDataGenerator
import pet.articles.tool.generator.ReviewPayloadTestDataGenerator
import pet.articles.tool.generator.ReviewTestDataGenerator
import pet.articles.tool.generator.TestDataGenerator
import pet.articles.tool.generator.UpdateArticlePayloadTestDataGenerator
import pet.articles.tool.generator.UserPayloadTestDataGenerator
import pet.articles.tool.generator.UserTestDataGenerator
import pet.articles.tool.producer.AuthenticationDetailsProducer
import pet.articles.tool.producer.AuthenticationDetailsProducerImpl

fun KoinApplication.testConfigure() {
    configure()
    modules(
        configModule,
        toolModule
    )
}

val configModule = module {
    single<DSLContext> { JooqConfig().dslContext(get(), SQLDialect.H2) }
}

val toolModule = module {
    single<DBCleaner> { DBCleanerImpl(get()) }

    single<TestDataGenerator<User>>(named("UserTestDataGenerator")) {
        UserTestDataGenerator(get())
    }
    single<TestDataGenerator<Article>>(named("ArticleTestDataGenerator")) {
        ArticleTestDataGenerator(get(), get(named("UserTestDataGenerator")))
    }
    single<TestDataGenerator<Review>>(named("ReviewTestDataGenerator")) {
        ReviewTestDataGenerator(
            get(),
            get(named("UserTestDataGenerator")),
            get(named("ArticleTestDataGenerator"))
        )
    }
    single<TestDataGenerator<RegistrationPayload>>(named("RegistrationPayloadTestDataGenerator")) {
        RegistrationPayloadTestDataGenerator(
            get(named("UserTestDataGenerator"))
        )
    }
    single<TestDataGenerator<UserPayload>>(named("UserPayloadTestDataGenerator")) {
        UserPayloadTestDataGenerator(
            get(named("UserTestDataGenerator"))
        )
    }
    single<TestDataGenerator<NewArticlePayload>>(named("NewArticlePayloadTestDataGenerator")) {
        NewArticlePayloadTestDataGenerator(
            get(named("ArticleTestDataGenerator")),
            get(named("UserTestDataGenerator"))
        )
    }
    single<TestDataGenerator<UpdateArticlePayload>>(named("UpdateArticlePayloadTestDataGenerator")) {
        UpdateArticlePayloadTestDataGenerator(
            get(named("ArticleTestDataGenerator")),
        )
    }
    single<TestDataGenerator<ReviewPayload>>(named("ReviewPayloadTestDataGenerator")) {
        ReviewPayloadTestDataGenerator(
            get(named("ReviewTestDataGenerator"))
        )
    }

    single<AuthenticationDetailsProducer> {
        AuthenticationDetailsProducerImpl(
            get(named("UserTestDataGenerator")),
            get())
    }
}