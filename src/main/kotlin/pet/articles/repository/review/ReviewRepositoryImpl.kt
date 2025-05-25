package pet.articles.repository.review

import org.jooq.DSLContext
import org.jooq.RecordMapper
import org.jooq.RecordUnmapper
import org.jooq.Record
import org.jooq.exception.DataAccessException

import pet.articles.generated.jooq.tables.records.ReviewsRecord
import pet.articles.generated.jooq.tables.references.REVIEWS
import pet.articles.model.dto.Review
import pet.articles.repository.CrudJooqRepository

class ReviewRepositoryImpl(
    private val dsl: DSLContext,
    private val reviewRecordMapper: RecordMapper<Record, Review>,
    reviewRecordUnmapper: RecordUnmapper<Review, ReviewsRecord>
) : ReviewRepository, CrudJooqRepository<Review, ReviewsRecord>(
    dsl = dsl,
    table = REVIEWS,
    recordMapper = reviewRecordMapper,
    recordUnmapper = reviewRecordUnmapper,
    idField = REVIEWS.ID
) {

    override fun updateById(item: Review, id: Int): Review =
        dsl.transactionResult { config ->
            config.dsl()
                .update(REVIEWS)
                .set(REVIEWS.TYPE, item.type.ordinal.toShort())
                .set(REVIEWS.CONTENT, item.content)
                .where(REVIEWS.ID.eq(id))
                .returning()
                .fetchOne()
                ?.map(reviewRecordMapper)
                ?: throw DataAccessException("Review with id = $id was not updated")
        }

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