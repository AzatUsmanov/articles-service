package pet.articles.tool.jooq.unmapper

import org.jooq.RecordUnmapper

import org.koin.core.annotation.Single

import pet.articles.generated.jooq.tables.records.AuthorshipOfArticlesRecord
import pet.articles.model.dto.AuthorshipOfArticle

@Single
class AuthorshipRecordUnmapper : RecordUnmapper<AuthorshipOfArticle, AuthorshipOfArticlesRecord> {

    override fun unmap(source: AuthorshipOfArticle?): AuthorshipOfArticlesRecord =
        AuthorshipOfArticlesRecord(
            authorId = source!!.authorId,
            articleId = source.articleId
        )
}
