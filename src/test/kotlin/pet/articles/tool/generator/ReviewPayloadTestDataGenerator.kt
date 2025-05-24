package pet.articles.tool.generator

import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

import pet.articles.model.dto.Review
import pet.articles.model.dto.payload.ReviewPayload

class ReviewPayloadTestDataGenerator(
    private val reviewGenerator: TestDataGenerator<Review>
) : TestDataGenerator<ReviewPayload> {

    override fun generateUnsavedData(dataSize: Int): List<ReviewPayload> =
        reviewGenerator.generateUnsavedData(dataSize).map(::convertToReviewPayload)

    override fun generateSavedData(dataSize: Int): List<ReviewPayload> =
        reviewGenerator.generateSavedData(dataSize).map(::convertToReviewPayload)

    override fun generateInvalidData(): ReviewPayload =
        convertToReviewPayload(reviewGenerator.generateInvalidData())

    private fun convertToReviewPayload(review: Review): ReviewPayload =
        ReviewPayload(
            type = review.type,
            content = review.content,
            authorId = review.authorId!!,
            articleId = review.articleId
        )
}