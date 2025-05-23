package pet.articles.service

import pet.articles.model.dto.Review

interface ReviewService {

    fun create(review: Review): Review

    fun deleteById(id: Int)

    fun findById(id: Int): Review?

    fun findByAuthorId(authorId: Int): List<Review>

    fun findByArticleId(articleId: Int): List<Review>
}