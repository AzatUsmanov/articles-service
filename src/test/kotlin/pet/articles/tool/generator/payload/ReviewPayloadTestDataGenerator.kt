package pet.articles.tool.generator.payload

import pet.articles.model.dto.Review
import pet.articles.model.dto.payload.ReviewPayload
import pet.articles.tool.generator.TestDataGenerator

class ReviewPayloadTestDataGenerator(
    reviewGenerator: TestDataGenerator<Review>
) : PayloadTestDataGeneratorBaseImpl<ReviewPayload, Review>(
    convertToPayload = ::convertToReviewPayload,
    modelTestDataGenerator = reviewGenerator
) {

    companion object {
        private fun convertToReviewPayload(review: Review): ReviewPayload =
            ReviewPayload(
                type = review.type,
                content = review.content,
                authorId = review.authorId!!,
                articleId = review.articleId
            )
    }
}