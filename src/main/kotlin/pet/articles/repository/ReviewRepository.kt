package pet.articles.repository

import pet.articles.model.dto.Review

interface ReviewRepository : CrudRepository<Review> {

    fun findByAuthorId(authorId: Int): List<Review>

    fun findByArticleId(articleId: Int): List<Review>
}