package pet.articles.repository

import org.jooq.DSLContext
import org.jooq.exception.DataAccessException
import org.koin.core.annotation.Single

import pet.articles.generated.jooq.tables.references.AUTHORSHIP_OF_ARTICLES
import pet.articles.model.dto.AuthorshipOfArticle

@Single
class AuthorshipOfArticleRepositoryImpl(
    private val dsl: DSLContext,
) : AuthorshipOfArticleRepository {

    override fun findAuthorIdsByArticleId(articleId: Int): List<Int> =
        dsl.select(AUTHORSHIP_OF_ARTICLES.AUTHOR_ID)
            .from(AUTHORSHIP_OF_ARTICLES)
            .where(AUTHORSHIP_OF_ARTICLES.ARTICLE_ID.eq(articleId))
            .fetch(AUTHORSHIP_OF_ARTICLES.AUTHOR_ID)
            .map { it ?: throw DataAccessException("received null author id") }

    override fun findArticleIdsByAuthorId(authorId: Int): List<Int> =
        dsl.select(AUTHORSHIP_OF_ARTICLES.ARTICLE_ID)
            .from(AUTHORSHIP_OF_ARTICLES)
            .where(AUTHORSHIP_OF_ARTICLES.AUTHOR_ID.eq(authorId))
            .fetch(AUTHORSHIP_OF_ARTICLES.ARTICLE_ID)
            .map { it ?: throw DataAccessException("received null article id") }

    override fun exists(authorshipOfArticle: AuthorshipOfArticle): Boolean =
        dsl.fetchExists(
            AUTHORSHIP_OF_ARTICLES,
            AUTHORSHIP_OF_ARTICLES.ARTICLE_ID.eq(authorshipOfArticle.articleId),
            AUTHORSHIP_OF_ARTICLES.AUTHOR_ID.eq(authorshipOfArticle.authorId)
        )
}