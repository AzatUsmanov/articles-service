package pet.articles.service.article

import pet.articles.model.dto.Article
import pet.articles.service.CrudService

interface ArticleService : CrudService<Article> {

    fun create(article: Article, authorIds: List<Int>): Article

    fun findArticlesByAuthorId(authorId: Int): List<Article>
}