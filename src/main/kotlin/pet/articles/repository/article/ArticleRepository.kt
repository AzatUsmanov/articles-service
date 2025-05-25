package pet.articles.repository.article

import pet.articles.model.dto.Article
import pet.articles.repository.CrudRepository

interface ArticleRepository : CrudRepository<Article> {

    fun save(article: Article, authorIds: List<Int>): Article
}