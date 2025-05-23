package pet.articles.repository

import pet.articles.model.dto.Article
import pet.articles.tool.annotation.NotEmpty

interface ArticleRepository {

    fun save(article: Article, authorIds: List<Int>): Article

    fun updateById(article: Article, id: Int): Article

    fun deleteById(id: Int)

    fun findById(id: Int): Article?

    fun findByIds(@NotEmpty ids: List<Int>): List<Article>

    fun findAll(): List<Article>

    fun existsById(id: Int): Boolean = findById(id) != null
}