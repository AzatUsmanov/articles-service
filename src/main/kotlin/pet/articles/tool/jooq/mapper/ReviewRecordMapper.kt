package pet.articles.tool.jooq.mapper

import org.jooq.Record
import org.jooq.RecordMapper

import org.koin.core.annotation.Single

import pet.articles.generated.jooq.tables.references.REVIEWS
import pet.articles.model.dto.Review
import pet.articles.model.enums.ReviewType

class ReviewRecordMapper : RecordMapper<Record, Review> {

    override fun map(record: Record): Review =
        Review(
            id = record[REVIEWS.ID],
            dateOfCreation = record[REVIEWS.DATE_OF_CREATION]!!,
            content = record[REVIEWS.CONTENT]!!,
            authorId = record[REVIEWS.AUTHOR_ID],
            articleId = record[REVIEWS.ARTICLE_ID]!!,
            type = record[REVIEWS.TYPE]!!
                .toInt()
                .let { typeIndex -> ReviewType.entries[typeIndex] }
        )
}
