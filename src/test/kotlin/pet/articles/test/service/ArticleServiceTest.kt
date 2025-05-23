package pet.articles.test.service

import net.datafaker.Faker
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.output.MigrateResult
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.RecordMapper
import org.jooq.RecordUnmapper
import org.jooq.SQLDialect
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.core.Koin
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.fileProperties
import org.koin.ksp.generated.module
import org.koin.test.KoinTest
import org.koin.test.get
import org.koin.test.inject
import org.koin.test.junit5.KoinTestExtension
import pet.articles.ArticlesServiceApplication
import pet.articles.config.DataSourceConfig
import pet.articles.config.FlywayConfig
import pet.articles.config.JooqConfig
import pet.articles.config.configModule
import pet.articles.config.configure
import pet.articles.config.repositoryModule
import pet.articles.config.serviceModule
import pet.articles.config.toolModule
import pet.articles.generated.jooq.tables.records.ArticlesRecord
import pet.articles.generated.jooq.tables.records.AuthorshipOfArticlesRecord
import pet.articles.generated.jooq.tables.records.UsersRecord

import pet.articles.model.dto.Article
import pet.articles.model.dto.AuthorshipOfArticle
import pet.articles.model.dto.User
import pet.articles.repository.ArticleRepository
import pet.articles.repository.ArticleRepositoryImpl
import pet.articles.repository.AuthorshipOfArticleRepository
import pet.articles.repository.AuthorshipOfArticleRepositoryImpl
import pet.articles.repository.UserRepository
import pet.articles.repository.UserRepositoryImpl
import pet.articles.service.ArticleService
import pet.articles.service.ArticleServiceImpl
import pet.articles.service.UserService
import pet.articles.service.UserServiceImpl
import pet.articles.test.ArticlesServiceApplicationTests
import pet.articles.test.testConfigure
import pet.articles.test.tool.db.DBCleaner
import pet.articles.test.tool.db.DBCleanerImpl
import pet.articles.test.tool.generator.ArticleTestDataGenerator
import pet.articles.test.tool.generator.TestDataGenerator
import pet.articles.test.tool.generator.UserTestDataGenerator
import pet.articles.tool.jooq.mapper.ArticleRecordMapper
import pet.articles.tool.jooq.mapper.UserRecordMapper
import pet.articles.tool.jooq.unmapper.ArticleRecordUnmapper
import pet.articles.tool.jooq.unmapper.AuthorshipRecordUnmapper
import pet.articles.tool.jooq.unmapper.UserRecordUnmapper

import java.util.NoSuchElementException
import javax.sql.DataSource

class ArticleServiceTest : KoinTest {

    companion object {
        const val NUM_OF_TEST_ARTICLES = 10
    }

    private val dbCleaner: DBCleaner by inject()

    private val articleService: ArticleService by inject()

    private val userService: UserService by inject()
    
    private val userTestDataGenerator: TestDataGenerator<User> by inject(
        named("UserTestDataGenerator")
    )

    private val articleTestDataGenerator: TestDataGenerator<Article> by inject(
        named("ArticleTestDataGenerator")
    )

    @JvmField
    @RegisterExtension
    val koinTestExtension = KoinTestExtension.create {
        testConfigure()
    }

    @AfterEach
    fun cleanDb() {
        dbCleaner.cleanUp()
    }

    @Test
    fun createArticle() {
        val a = articleTestDataGenerator

        val articleForSave: Article = articleTestDataGenerator.generateUnsavedData()
        val savedAuthors: List<User> = userTestDataGenerator.generateSavedData(NUM_OF_TEST_ARTICLES)
        val authorIds: List<Int> = savedAuthors.map { it.id!! }

        val savedArticle: Article = articleService.create(articleForSave, authorIds)

        val articleForCheck: Article? = articleService.findById(savedArticle.id!!)
        val authorsForCheck: List<User> = userService.findAuthorsByArticleId(savedArticle.id!!)
        assertNotNull(articleForCheck)
        assertEquals(savedArticle, articleForCheck)
        assertEquals(savedAuthors.size, authorsForCheck.size)
        assertTrue(savedAuthors.containsAll(authorsForCheck))
        assertTrue(authorsForCheck.containsAll(savedAuthors))
    }

    @Test
    fun createArticleWithoutAuthors() {
        val articleForSave: Article = articleTestDataGenerator.generateUnsavedData()

        val savedArticle: Article = articleService.create(articleForSave, emptyList())

        val articleForCheck: Article? = articleService.findById(savedArticle.id!!)
        assertNotNull(articleForCheck)
        assertEquals(savedArticle, articleForCheck)
    }

    @Test
    fun saveArticleWithInvalidData() {
        val invalidArticle: Article = articleTestDataGenerator.generateInvalidData()

        assertThrows(RuntimeException::class.java) {
            articleService.create(invalidArticle, emptyList())
        }
    }

    @Test
    fun updateArticleById() {
        val savedArticle: Article = articleTestDataGenerator.generateSavedData()
        val articleDataForUpdate: Article = articleTestDataGenerator.generateUnsavedData()

        val updatedArticle: Article = articleService.updateById(articleDataForUpdate, savedArticle.id!!)

        val articleForCheck: Article? = articleService.findById(savedArticle.id!!)
        assertNotNull(articleForCheck)
        assertEquals(updatedArticle, articleForCheck)
    }

    @Test
    fun updateArticleByIdWithInvalidData() {
        val savedArticle: Article = articleTestDataGenerator.generateSavedData()
        val invalidArticle: Article = articleTestDataGenerator.generateInvalidData()

        assertThrows(RuntimeException::class.java) {
            articleService.updateById(invalidArticle, savedArticle.id!!)
        }
    }

    @Test
    fun updateArticleByNonExistentId() {
        val articleDataForUpdate: Article = articleTestDataGenerator.generateUnsavedData()

        assertThrows(NoSuchElementException::class.java) {
            articleService.updateById(articleDataForUpdate, articleDataForUpdate.id!!)
        }
    }

    @Test
    fun deleteArticleById() {
        val savedArticle: Article = articleTestDataGenerator.generateSavedData()

        articleService.deleteById(savedArticle.id!!)

        val articleForCheck: Article? = articleService.findById(savedArticle.id!!)
        assertNull(articleForCheck)
    }

    @Test
    fun deleteArticleNonExistentId() {
        val unsavedArticle: Article = articleTestDataGenerator.generateUnsavedData()

        articleService.deleteById(unsavedArticle.id!!)

        val articleForCheck: Article? = articleService.findById(unsavedArticle.id!!)
        assertNull(articleForCheck)
    }

    @Test
    fun findArticleById() {
        val savedArticle: Article = articleTestDataGenerator.generateSavedData()

        val articleForCheck: Article? = articleService.findById(savedArticle.id!!)

        assertNotNull(articleForCheck)
        assertEquals(savedArticle, articleForCheck)
    }

    @Test
    fun findArticleByNonExistentId() {
        val unsavedArticle: Article = articleTestDataGenerator.generateUnsavedData()

        val articleForCheck: Article? = articleService.findById(unsavedArticle.id!!)

        assertNull(articleForCheck)
    }

    @Test
    fun findArticlesByAuthorId() {
        val savedAuthor: User = userTestDataGenerator.generateSavedData()
        val unsavedArticles: List<Article> = articleTestDataGenerator.generateUnsavedData(NUM_OF_TEST_ARTICLES)
        val savedArticles: List<Article> = unsavedArticles.map {
            articleService.create(it, listOf(savedAuthor.id!!))
        }

        val articlesForCheck: List<Article> = articleService.findArticlesByAuthorId(savedAuthor.id!!)
        assertEquals(savedArticles.size, articlesForCheck.size)
        assertTrue(articlesForCheck.containsAll(savedArticles))
        assertTrue(savedArticles.containsAll(articlesForCheck))
    }

    @Test
    fun findArticlesByNonExistentAuthorId() {
        val unsavedAuthor: User = userTestDataGenerator.generateUnsavedData()

        val articles: List<Article> = articleService.findArticlesByAuthorId(unsavedAuthor.id!!)

        assertTrue(articles.isEmpty())
    }

    @Test
    fun findAllArticles() {
        val allArticles: List<Article> = articleTestDataGenerator.generateSavedData(NUM_OF_TEST_ARTICLES)

        val articlesForCheck: List<Article> = articleService.findAll()

        assertEquals(allArticles.size, articlesForCheck.size)
        assertTrue(allArticles.containsAll(articlesForCheck))
        assertTrue(articlesForCheck.containsAll(allArticles))
    }
}