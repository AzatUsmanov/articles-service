package pet.articles.service.review

import pet.articles.model.dto.Review
import pet.articles.service.CrudService

interface ReviewService : CrudService<Review> {

    fun findByAuthorId(authorId: Int): List<Review>

    fun findByArticleId(articleId: Int): List<Review>
}