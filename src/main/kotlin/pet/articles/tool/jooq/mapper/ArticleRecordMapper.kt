package pet.articles.tool.jooq.mapper

import org.jooq.Record
import org.jooq.RecordMapper

import org.koin.core.annotation.Single

import pet.articles.generated.jooq.tables.references.ARTICLES
import pet.articles.model.dto.Article

class ArticleRecordMapper : RecordMapper<Record, Article> {

    override fun map(record: Record): Article =
        Article(
            id = record[ARTICLES.ID],
            dateOfCreation = record[ARTICLES.DATE_OF_CREATION]!!,
            topic = record[ARTICLES.TOPIC]!!,
            content = record[ARTICLES.CONTENT]!!
        )
}