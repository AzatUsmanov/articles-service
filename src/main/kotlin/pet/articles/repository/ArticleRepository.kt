package pet.articles.repository

import pet.articles.model.dto.Article

interface ArticleRepository : CrudRepository<Article> {

    fun save(article: Article, authorIds: List<Int>): Article
}