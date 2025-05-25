package pet.articles.service.article

import org.koin.core.logger.Logger

import pet.articles.model.dto.Article
import pet.articles.repository.article.ArticleRepository
import pet.articles.repository.authorship.AuthorshipOfArticleRepository
import pet.articles.service.CrudServiceBaseImpl

class ArticleServiceImpl(
    private val articleRepository: ArticleRepository,
    private val authorshipRepository: AuthorshipOfArticleRepository,
    log: Logger
) : ArticleService, CrudServiceBaseImpl<Article>(
    crudRepository = articleRepository,
    log = log
) {

    override fun create(article: Article, authorIds: List<Int>): Article =
        articleRepository.save(article, authorIds)

    override fun create(item: Article): Article {
        throw UnsupportedOperationException("This operation is not supported. " +
                "Use create(article: Article, authorIds: List<Int>): Article")
    }

    override fun findArticlesByAuthorId(authorId: Int): List<Article> {
        val articleIds: List<Int> = authorshipRepository.findArticleIdsByAuthorId(authorId)
        return findByIds(articleIds)
    }
}