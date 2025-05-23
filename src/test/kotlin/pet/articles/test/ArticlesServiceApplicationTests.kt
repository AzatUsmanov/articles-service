package pet.articles.test

import net.datafaker.providers.entertainment.NewGirl
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.junit.jupiter.api.Test
import org.koin.core.Koin
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.dsl.module
import pet.articles.config.JooqConfig
import pet.articles.config.configure

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.fileProperties
import org.mindrot.jbcrypt.BCrypt
import pet.articles.model.dto.Article
import pet.articles.model.dto.Review
import pet.articles.model.dto.User
import pet.articles.model.dto.payload.NewArticlePayload
import pet.articles.model.dto.payload.RegistrationPayload
import pet.articles.model.dto.payload.ReviewPayload
import pet.articles.model.dto.payload.UpdateArticlePayload
import pet.articles.model.dto.payload.UserPayload
import pet.articles.test.tool.db.DBCleaner
import pet.articles.test.tool.db.DBCleanerImpl
import pet.articles.test.tool.generator.ArticleTestDataGenerator
import pet.articles.test.tool.generator.NewArticlePayloadTestDataGenerator
import pet.articles.test.tool.generator.RegistrationPayloadTestDataGenerator
import pet.articles.test.tool.generator.ReviewPayloadTestDataGenerator
import pet.articles.test.tool.generator.ReviewTestDataGenerator
import pet.articles.test.tool.generator.TestDataGenerator
import pet.articles.test.tool.generator.UpdateArticlePayloadTestDataGenerator
import pet.articles.test.tool.generator.UserPayloadTestDataGenerator
import pet.articles.test.tool.generator.UserTestDataGenerator
import pet.articles.test.tool.producer.AuthenticationDetailsProducer
import pet.articles.test.tool.producer.AuthenticationDetailsProducerImpl


@Module
@ComponentScan
class ArticlesServiceApplicationTests {

	@Test
	fun contextLoads() {
	}
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

fun KoinApplication.testConfigure() {
	configure()
	modules(
		configModule,
		toolModule
	)
}
