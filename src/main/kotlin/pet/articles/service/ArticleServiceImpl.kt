package pet.articles.service

import org.koin.core.annotation.Single

import pet.articles.model.dto.Article
import pet.articles.repository.ArticleRepository
import pet.articles.repository.AuthorshipOfArticleRepository

class ArticleServiceImpl(
    private val articleRepository: ArticleRepository,
    private val authorshipOfArticleRepository: AuthorshipOfArticleRepository
) : ArticleService {

    override fun create(article: Article, authorIds: List<Int>): Article = articleRepository.save(article, authorIds)

    override fun updateById(article: Article, id: Int): Article {
        if (!existsById(id)) {
            throw NoSuchElementException("Attempt to update article by non existent id = $id")
        }
        return articleRepository.updateById(article, id)
    }

    override fun deleteById(id: Int) = articleRepository.deleteById(id)

    override fun findById(id: Int): Article? = articleRepository.findById(id)

    override fun findArticlesByAuthorId(authorId: Int): List<Article> {
        val articleIds: List<Int> = authorshipOfArticleRepository.findArticleIdsByAuthorId(authorId)
        return findByIds(articleIds)
    }

    override fun findByIds(articleIds: List<Int>): List<Article> =
        if (articleIds.isNotEmpty()) articleRepository.findByIds(articleIds) else emptyList()

    override fun findAll(): List<Article> = articleRepository.findAll()

    override fun existsById(id: Int): Boolean = articleRepository.existsById(id)
}