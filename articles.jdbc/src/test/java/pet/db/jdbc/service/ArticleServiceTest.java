package pet.db.jdbc.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import pet.db.jdbc.entity.Article;
import pet.db.jdbc.entity.User;
import pet.db.jdbc.tool.db.DbCleaner;
import pet.db.jdbc.tool.generator.TestDataGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class ArticleServiceTest {

    @Autowired
    private DbCleaner dbCleaner;

    @Autowired
    private ArticleService articleService;

    @Autowired
    private UserService userService;

    @Autowired
    private TestDataGenerator<User> userTestDataGenerator;

    @Autowired
    private TestDataGenerator<Article> articleTestDataGenerator;

    @AfterEach
    void cleanDB() {
        dbCleaner.cleanAll();
    }

    @Test
    public void createArticle() {
        Article articleForSave = articleTestDataGenerator.generateUnsavedData();
        List<User> savedAuthors = userTestDataGenerator.generateSavedData(10);
        List<Integer> authorIds = savedAuthors.stream()
                .map(User::getId)
                .toList();


        Article savedArticle = articleService.create(articleForSave, authorIds);


        Optional<Article> articleOptionalForCheck = articleService.findById(savedArticle.getId());
        List<User> authorsForCheck = userService.findAuthorsByArticleId(savedArticle.getId());

        assertTrue(articleOptionalForCheck.isPresent());
        assertEquals(savedArticle, articleOptionalForCheck.get());
        assertEquals(savedAuthors.size(), authorsForCheck.size());
        assertTrue(savedAuthors.containsAll(authorsForCheck));
        assertTrue(authorsForCheck.containsAll(savedAuthors));
    }

    @Test
    public void createArticleWithoutAuthors() {
        Article articleForSave = articleTestDataGenerator.generateUnsavedData();

        Article savedArticle = articleService.create(articleForSave, new ArrayList<>());


        Optional<Article> articleOptionalForCheck = articleService.findById(savedArticle.getId());

        assertTrue(articleOptionalForCheck.isPresent());
        assertEquals(savedArticle, articleOptionalForCheck.get());
    }

    @Test
    public void saveUserWithInvalidData() {
        Article articleForSave = articleTestDataGenerator.generateUnsavedData();
        articleForSave.setContent(null);

        assertThrows(RuntimeException.class,
                () -> articleService.create(articleForSave, new ArrayList<>()));
    }

    @Test
    public void updateArticleById() {
        Article savedArticle = articleTestDataGenerator.generateSavedData();
        Article articleDataForUpdate = articleTestDataGenerator.generateUnsavedData();

        Article updatedArticle = articleService.updateById(articleDataForUpdate, savedArticle.getId());

        Optional<Article> articleOptionalForCheck = articleService.findById(savedArticle.getId());
        assertTrue(articleOptionalForCheck.isPresent());
        assertEquals(updatedArticle, articleOptionalForCheck.get());
    }

    @Test
    public void updateArticleByNonExistentId() {
        Article articleDataForUpdate = articleTestDataGenerator.generateUnsavedData();

        assertThrows(NoSuchElementException.class,
                () -> articleService.updateById(articleDataForUpdate, articleDataForUpdate.getId()));
    }

    @Test
    public void deleteArticleById() {
        Article savedArticle = articleTestDataGenerator.generateSavedData();

        articleService.deleteById(savedArticle.getId());

        Optional<Article> articleOptionalForCheck = articleService.findById(savedArticle.getId());
        assertTrue(articleOptionalForCheck.isEmpty());
    }

    @Test
    public void deleteArticleNonExistentId() {
        Article unsavedArticle = articleTestDataGenerator.generateUnsavedData();

        articleService.deleteById(unsavedArticle.getId());

        Optional<Article> articleOptionalForCheck = articleService.findById(unsavedArticle.getId());
        assertTrue(articleOptionalForCheck.isEmpty());
    }

    @Test
    public void findArticleById() {
        Article savedArticle = articleTestDataGenerator.generateSavedData();

        Optional<Article> articleOptionalForCheck = articleService.findById(savedArticle.getId());

        assertTrue(articleOptionalForCheck.isPresent());
        assertEquals(savedArticle, articleOptionalForCheck.get());
    }

    @Test
    public void findArticleByNonExistentId() {
        Article unsavedArticle = articleTestDataGenerator.generateUnsavedData();

        Optional<Article> articleOptionalForCheck = articleService.findById(unsavedArticle.getId());

        assertTrue(articleOptionalForCheck.isEmpty());
    }

    @Test
    public void findArticlesByAuthorId() {
        User savedAuthor = userTestDataGenerator.generateSavedData();
        List<Article> unsavedArticles = articleTestDataGenerator.generateUnsavedData(10);
        List<Article> savedArticles = unsavedArticles.stream()
                .map(x -> articleService.create(x, List.of(savedAuthor.getId())))
                .toList();

        List<Article> articlesForCheck = articleService.findArticlesByAuthorId(savedAuthor.getId());

        assertEquals(savedArticles.size(), articlesForCheck.size());
        assertTrue(articlesForCheck.containsAll(savedArticles));
        assertTrue(savedArticles.containsAll(articlesForCheck));
    }

    @Test
    public void findArticlesByNonExistentAuthorId() {
        User unsavedAuthor = userTestDataGenerator.generateUnsavedData();

        assertThrows(NoSuchElementException.class,
                () -> articleService.findArticlesByAuthorId(unsavedAuthor.getId()));
    }

    @Test
    public void findAllArticles() {
        List<Article> allArticles = articleTestDataGenerator.generateSavedData(10);

        List<Article> articlesForCheck = articleService.findAll();

        assertEquals(allArticles.size(), articlesForCheck.size());
        assertTrue(allArticles.containsAll(articlesForCheck));
        assertTrue(articlesForCheck.containsAll(allArticles));
    }

}
