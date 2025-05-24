package pet.articles.service

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.core.qualifier.named
import org.koin.test.KoinTest
import org.koin.test.inject
import org.koin.test.junit5.KoinTestExtension

import pet.articles.model.dto.Article
import pet.articles.model.dto.User
import pet.articles.service.ArticleService
import pet.articles.service.UserService
import pet.articles.config.testConfigure
import pet.articles.tool.db.DBCleaner
import pet.articles.tool.generator.TestDataGenerator
import pet.articles.tool.testing.extension.DBCleanupExtension
import pet.articles.tool.testing.extension.KoinConfigureTestExtension

import java.util.NoSuchElementException

@ExtendWith(DBCleanupExtension::class)
@ExtendWith(KoinConfigureTestExtension::class)
class ArticleServiceTest : KoinTest {

    companion object {
        const val NUM_OF_TEST_ARTICLES = 10
    }

    private val articleService: ArticleService by inject()

    private val userService: UserService by inject()
    
    private val userGenerator: TestDataGenerator<User> by inject(
        named("UserTestDataGenerator")
    )

    private val articleGenerator: TestDataGenerator<Article> by inject(
        named("ArticleTestDataGenerator")
    )

    @Test
    fun createArticle() {
        val a = articleGenerator

        val articleForSave: Article = articleGenerator.generateUnsavedData()
        val savedAuthors: List<User> = userGenerator.generateSavedData(NUM_OF_TEST_ARTICLES)
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
        val articleForSave: Article = articleGenerator.generateUnsavedData()

        val savedArticle: Article = articleService.create(articleForSave, emptyList())

        val articleForCheck: Article? = articleService.findById(savedArticle.id!!)
        assertNotNull(articleForCheck)
        assertEquals(savedArticle, articleForCheck)
    }

    @Test
    fun saveArticleWithInvalidData() {
        val invalidArticle: Article = articleGenerator.generateInvalidData()

        assertThrows(RuntimeException::class.java) {
            articleService.create(invalidArticle, emptyList())
        }
    }

    @Test
    fun updateArticleById() {
        val savedArticle: Article = articleGenerator.generateSavedData()
        val articleDataForUpdate: Article = articleGenerator.generateUnsavedData()

        val updatedArticle: Article = articleService.updateById(articleDataForUpdate, savedArticle.id!!)

        val articleForCheck: Article? = articleService.findById(savedArticle.id!!)
        assertNotNull(articleForCheck)
        assertEquals(updatedArticle, articleForCheck)
    }

    @Test
    fun updateArticleByIdWithInvalidData() {
        val savedArticle: Article = articleGenerator.generateSavedData()
        val invalidArticle: Article = articleGenerator.generateInvalidData()

        assertThrows(RuntimeException::class.java) {
            articleService.updateById(invalidArticle, savedArticle.id!!)
        }
    }

    @Test
    fun updateArticleByNonExistentId() {
        val articleDataForUpdate: Article = articleGenerator.generateUnsavedData()

        assertThrows(NoSuchElementException::class.java) {
            articleService.updateById(articleDataForUpdate, articleDataForUpdate.id!!)
        }
    }

    @Test
    fun deleteArticleById() {
        val savedArticle: Article = articleGenerator.generateSavedData()

        articleService.deleteById(savedArticle.id!!)

        val articleForCheck: Article? = articleService.findById(savedArticle.id!!)
        assertNull(articleForCheck)
    }

    @Test
    fun deleteArticleNonExistentId() {
        val unsavedArticle: Article = articleGenerator.generateUnsavedData()

        articleService.deleteById(unsavedArticle.id!!)

        val articleForCheck: Article? = articleService.findById(unsavedArticle.id!!)
        assertNull(articleForCheck)
    }

    @Test
    fun findArticleById() {
        val savedArticle: Article = articleGenerator.generateSavedData()

        val articleForCheck: Article? = articleService.findById(savedArticle.id!!)

        assertNotNull(articleForCheck)
        assertEquals(savedArticle, articleForCheck)
    }

    @Test
    fun findArticleByNonExistentId() {
        val unsavedArticle: Article = articleGenerator.generateUnsavedData()

        val articleForCheck: Article? = articleService.findById(unsavedArticle.id!!)

        assertNull(articleForCheck)
    }

    @Test
    fun findArticlesByAuthorId() {
        val savedAuthor: User = userGenerator.generateSavedData()
        val unsavedArticles: List<Article> = articleGenerator.generateUnsavedData(NUM_OF_TEST_ARTICLES)
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
        val unsavedAuthor: User = userGenerator.generateUnsavedData()

        val articles: List<Article> = articleService.findArticlesByAuthorId(unsavedAuthor.id!!)

        assertTrue(articles.isEmpty())
    }

    @Test
    fun findAllArticles() {
        val allArticles: List<Article> = articleGenerator.generateSavedData(NUM_OF_TEST_ARTICLES)

        val articlesForCheck: List<Article> = articleService.findAll()

        assertEquals(allArticles.size, articlesForCheck.size)
        assertTrue(allArticles.containsAll(articlesForCheck))
        assertTrue(articlesForCheck.containsAll(allArticles))
    }
}