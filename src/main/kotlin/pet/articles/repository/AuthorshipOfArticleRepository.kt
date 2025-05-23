package pet.articles.repository

import pet.articles.model.dto.AuthorshipOfArticle

interface AuthorshipOfArticleRepository {

    fun findAuthorIdsByArticleId(articleId: Int): List<Int>

    fun findArticleIdsByAuthorId(authorId: Int): List<Int>

    fun exists(authorshipOfArticle: AuthorshipOfArticle): Boolean
}
