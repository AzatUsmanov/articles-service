package pet.articles.repository

import org.jooq.DSLContext
import org.jooq.RecordMapper
import org.jooq.RecordUnmapper
import org.jooq.Record
import org.jooq.exception.DataAccessException

import org.koin.core.annotation.Single

import pet.articles.generated.jooq.tables.records.ArticlesRecord
import pet.articles.generated.jooq.tables.records.AuthorshipOfArticlesRecord
import pet.articles.generated.jooq.tables.records.UsersRecord
import pet.articles.generated.jooq.tables.references.ARTICLES
import pet.articles.generated.jooq.tables.references.REVIEWS
import pet.articles.generated.jooq.tables.references.USERS
import pet.articles.model.dto.Article
import pet.articles.model.dto.AuthorshipOfArticle
import pet.articles.model.dto.Review
import pet.articles.model.dto.User
import pet.articles.tool.extension.toUnit

class ArticleRepositoryImpl(
    private val dsl: DSLContext,
    private val authorshipOfArticlesRecordUnmapper: RecordUnmapper<AuthorshipOfArticle, AuthorshipOfArticlesRecord>,
    private val articleRecordMapper: RecordMapper<Record, Article>,
    articleRecordUnmapper: RecordUnmapper<Article, ArticlesRecord>
) : ArticleRepository, CrudJooqRepository<Article, ArticlesRecord>(
    dsl = dsl,
    table = ARTICLES,
    recordMapper = articleRecordMapper,
    recordUnmapper = articleRecordUnmapper,
    idField = ARTICLES.ID
) {

    override fun save(article: Article, authorIds: List<Int>): Article =
        dsl.transactionResult { config ->
            val savedArticle: Article = super.save(article)
            config.dsl()
                .batchInsert(
                    authorIds.asSequence()
                        .map { AuthorshipOfArticle(it, savedArticle.id!!) }
                        .map { authorshipOfArticlesRecordUnmapper.unmap(it) }
                        .toList()
                ).execute()

            savedArticle
        }

    override fun updateById(item: Article, id: Int): Article =
        dsl.transactionResult { config ->
            config.dsl()
                .update(ARTICLES)
                .set(ARTICLES.TOPIC, item.topic)
                .set(ARTICLES.CONTENT, item.content)
                .where(ARTICLES.ID.eq(id))
                .returning()
                .fetchOne()
                ?.map(articleRecordMapper)
                ?: throw DataAccessException("Article with id = $id was not updated")
        }
}