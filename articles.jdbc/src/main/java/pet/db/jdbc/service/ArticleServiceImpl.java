package pet.db.jdbc.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import pet.db.jdbc.entity.Article;
import pet.db.jdbc.entity.AuthorshipOfArticle;
import pet.db.jdbc.repository.ArticleRepository;
import pet.db.jdbc.repository.AuthorshipOfArticleRepository;
import pet.db.jdbc.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

    private final ArticleRepository articleRepository;

    private final UserRepository userRepository;

    private final AuthorshipOfArticleRepository authorshipOfArticleRepository;

    @Override
    public Article create(Article article, List<Integer> authorIds) {
        Article savedArticle = articleRepository.save(article);
        saveAuthorship(savedArticle, authorIds);
        return savedArticle;
    }

    @Override
    public Article updateById(Article article, Integer id) {
        if (!existsById(id)) {
            throw new NoSuchElementException(
                    "Attempt to update article by non existent id = %d".formatted(id));
        }
        return articleRepository.updateById(article, id);
    }

    @Override
    public void deleteById(Integer id) {
        articleRepository.deleteById(id);
    }

    @Override
    public Optional<Article> findById(Integer id) {
        return articleRepository.findById(id);
    }

    @Override
    public List<Article> findArticlesByAuthorId(Integer authorId) {
        if (!userRepository.existsById(authorId)) {
            throw new NoSuchElementException(
                    "Attempt to find list of articles by non existent user id = %d".formatted(authorId));
        }
        List<Integer> articleIds = authorshipOfArticleRepository.findArticleIdsByAuthorId(authorId);
        return findByIds(articleIds);
    }

    @Override
    public List<Article> findByIds(List<Integer> articleIds) {
        if (!articleIds.isEmpty()) {
            return articleRepository.findByIds(articleIds);
        }
        return new ArrayList<>();
    }

    @Override
    public List<Article> findAll() {
        return articleRepository.findAll();
    }

    @Override
    public boolean existsById(Integer id) {
        return articleRepository.existsById(id);
    }

    private void saveAuthorship(Article savedArticle, List<Integer> authorIds) {
        List<AuthorshipOfArticle> authorshipOfArticles = transformToListOfAuthorshipOfArticle(savedArticle, authorIds);
        authorshipOfArticleRepository.save(authorshipOfArticles);
    }

    private List<AuthorshipOfArticle> transformToListOfAuthorshipOfArticle(Article savedArticle, List<Integer> authorIds) {
        return authorIds.stream()
                .map(id -> new AuthorshipOfArticle(savedArticle.getId(), id))
                .toList();
    }
    
}
