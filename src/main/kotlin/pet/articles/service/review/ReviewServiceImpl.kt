package pet.articles.service.review

import org.koin.core.logger.Logger

import pet.articles.model.dto.Review
import pet.articles.repository.review.ReviewRepository
import pet.articles.service.CrudServiceBaseImpl

class ReviewServiceImpl(
    private val reviewRepository: ReviewRepository,
    log: Logger
) : ReviewService, CrudServiceBaseImpl<Review>(
    crudRepository = reviewRepository,
    log = log
) {

    override fun findByAuthorId(authorId: Int): List<Review> =
        reviewRepository.findByAuthorId(authorId)

    override fun findByArticleId(articleId: Int): List<Review> =
        reviewRepository.findByArticleId(articleId)
}