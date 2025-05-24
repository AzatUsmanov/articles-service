package pet.articles.repository

import org.jooq.DSLContext
import org.jooq.RecordMapper
import org.jooq.RecordUnmapper
import org.jooq.Record
import org.jooq.exception.DataAccessException

import org.koin.core.annotation.Single

import pet.articles.generated.jooq.tables.records.ArticlesRecord
import pet.articles.generated.jooq.tables.records.AuthorshipOfArticlesRecord
import pet.articles.generated.jooq.tables.references.ARTICLES
import pet.articles.model.dto.Article
import pet.articles.model.dto.AuthorshipOfArticle
import pet.articles.tool.extension.toUnit

class ArticleRepositoryImpl(
    private val dsl: DSLContext,
    private val articleRecordMapper: RecordMapper<Record, Article>,
    private val articleRecordUnmapper: RecordUnmapper<Article, ArticlesRecord>,
    private val authorshipOfArticlesRecordUnmapper: RecordUnmapper<AuthorshipOfArticle, AuthorshipOfArticlesRecord>
) : ArticleRepository {

    override fun save(article: Article, authorIds: List<Int>): Article =
        dsl.transactionResult { config ->
            val savedArticle: Article = config.dsl()
                .insertInto(ARTICLES)
                .set(articleRecordUnmapper.unmap(article))
                .returning()
                .fetchOne()
                ?.map(articleRecordMapper)
                ?: throw DataAccessException("article was not saved")

            config.dsl()
                .batchInsert(
                    authorIds.asSequence()
                        .map { AuthorshipOfArticle(it, savedArticle.id!!) }
                        .map { authorshipOfArticlesRecordUnmapper.unmap(it) }
                        .toList()
                ).execute()

            savedArticle
        }

    override fun updateById(article: Article, id: Int): Article =
        dsl.transactionResult { config ->
            config.dsl()
                .update(ARTICLES)
                .set(ARTICLES.TOPIC, article.topic)
                .set(ARTICLES.CONTENT, article.content)
                .where(ARTICLES.ID.eq(id))
                .returning()
                .fetchOne()
                ?.map(articleRecordMapper)
                ?: throw DataAccessException("article with id = $id was not updated")
        }

    override fun deleteById(id: Int) =
        dsl.delete(ARTICLES)
            .where(ARTICLES.ID.eq(id))
            .execute()
            .toUnit()

    override fun findById(id: Int): Article? =
        dsl.selectFrom(ARTICLES)
            .where(ARTICLES.ID.eq(id))
            .fetchOne()
            ?.map(articleRecordMapper)

    override fun findByIds(ids: List<Int>): List<Article> =
        dsl.selectFrom(ARTICLES)
            .where(ARTICLES.ID.`in`(ids))
            .fetch()
            .map(articleRecordMapper)

    override fun findAll(): List<Article> =
        dsl.selectFrom(ARTICLES)
            .fetch()
            .map(articleRecordMapper)
}