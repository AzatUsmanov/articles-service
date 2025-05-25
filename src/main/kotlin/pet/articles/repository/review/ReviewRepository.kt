package pet.articles.repository.review

import pet.articles.model.dto.Review
import pet.articles.repository.CrudRepository

interface ReviewRepository : CrudRepository<Review> {

    fun findByAuthorId(authorId: Int): List<Review>

    fun findByArticleId(articleId: Int): List<Review>
}