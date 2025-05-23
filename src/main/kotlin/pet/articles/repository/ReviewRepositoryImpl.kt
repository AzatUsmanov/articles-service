package pet.articles.repository

import org.jooq.DSLContext
import org.jooq.RecordMapper
import org.jooq.RecordUnmapper
import org.jooq.Record
import org.jooq.exception.DataAccessException

import org.koin.core.annotation.Single

import pet.articles.generated.jooq.tables.records.ReviewsRecord
import pet.articles.generated.jooq.tables.references.REVIEWS
import pet.articles.model.dto.Review
import pet.articles.tool.extension.toUnit

@Single
class ReviewRepositoryImpl(
    private val dsl: DSLContext,
    private val reviewRecordMapper: RecordMapper<Record, Review>,
    private val reviewRecordUnmapper: RecordUnmapper<Review, ReviewsRecord>
) : ReviewRepository {

    override fun save(review: Review): Review =
        dsl.transactionResult { config ->
            config.dsl()
                .insertInto(REVIEWS)
                .set(reviewRecordUnmapper.unmap(review))
                .returning()
                .fetchOne()
                ?.map(reviewRecordMapper)
                ?: throw DataAccessException("Review was not saved")
        }

    override fun deleteById(id: Int) =
        dsl.deleteFrom(REVIEWS)
            .where(REVIEWS.ID.eq(id))
            .execute()
            .toUnit()

    override fun findById(id: Int): Review? =
        dsl.selectFrom(REVIEWS)
            .where(REVIEWS.ID.eq(id))
            .fetchOne()
            ?.map(reviewRecordMapper)

    override fun findByAuthorId(authorId: Int): List<Review> =
        dsl.selectFrom(REVIEWS)
            .where(REVIEWS.AUTHOR_ID.eq(authorId))
            .fetch()
            .map(reviewRecordMapper)

    override fun findByArticleId(articleId: Int): List<Review> =
        dsl.selectFrom(REVIEWS)
            .where(REVIEWS.ARTICLE_ID.eq(articleId))
            .fetch()
            .map(reviewRecordMapper)
}