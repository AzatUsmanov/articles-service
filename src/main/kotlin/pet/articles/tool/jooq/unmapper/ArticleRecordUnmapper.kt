package pet.articles.tool.jooq.unmapper

import org.jooq.RecordUnmapper

import org.koin.core.annotation.Single

import pet.articles.generated.jooq.tables.records.ArticlesRecord
import pet.articles.model.dto.Article

class ArticleRecordUnmapper : RecordUnmapper<Article, ArticlesRecord> {

    override fun unmap(source: Article?): ArticlesRecord =
        ArticlesRecord(
            id = source!!.id,
            dateOfCreation = source.dateOfCreation,
            topic = source.topic,
            content = source.content
        )
}