package pet.articles.tool.jooq.unmapper;

import org.jooq.RecordUnmapper

import org.koin.core.annotation.Single

import pet.articles.generated.jooq.tables.records.ReviewsRecord
import pet.articles.model.dto.Review

class ReviewRecordUnmapper : RecordUnmapper<Review, ReviewsRecord> {

    override fun unmap(source: Review?): ReviewsRecord =
        ReviewsRecord(
            id = source!!.id,
            type = source.type.ordinal.toShort(),
            dateOfCreation = source.dateOfCreation,
            content = source.content,
            articleId = source.articleId,
            authorId = source.authorId
        )
}