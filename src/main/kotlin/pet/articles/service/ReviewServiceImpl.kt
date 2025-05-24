package pet.articles.service

import org.koin.core.annotation.Single

import pet.articles.model.dto.Review
import pet.articles.repository.ReviewRepository

class ReviewServiceImpl(
    private val reviewRepository: ReviewRepository,
) : ReviewService {

    override fun create(review: Review): Review = reviewRepository.save(review)

    override fun deleteById(id: Int) = reviewRepository.deleteById(id)

    override fun findById(id: Int): Review? = reviewRepository.findById(id)

    override fun findByAuthorId(authorId: Int): List<Review> = reviewRepository.findByAuthorId(authorId)

    override fun findByArticleId(articleId: Int): List<Review> = reviewRepository.findByArticleId(articleId)
}