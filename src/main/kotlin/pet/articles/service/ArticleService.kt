package pet.articles.service

import pet.articles.model.dto.Article

interface ArticleService {

    fun create(article: Article, authorIds: List<Int>): Article

    fun updateById(article: Article, id: Int): Article

    fun deleteById(id: Int)

    fun findById(id: Int): Article?

    fun findArticlesByAuthorId(authorId: Int): List<Article>

    fun findByIds(articleIds: List<Int>): List<Article>

    fun findAll(): List<Article>

    fun existsById(id: Int): Boolean
}