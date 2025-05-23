package pet.articles.repository

import pet.articles.model.dto.Review

interface ReviewRepository {

    fun save(review: Review): Review

    fun deleteById(id: Int)

    fun findById(id: Int): Review?

    fun findByAuthorId(authorId: Int): List<Review>

    fun findByArticleId(articleId: Int): List<Review>
}