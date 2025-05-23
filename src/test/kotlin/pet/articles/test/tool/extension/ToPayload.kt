package pet.articles.test.tool.extension

import pet.articles.model.dto.Article
import pet.articles.model.dto.Review
import pet.articles.model.dto.User
import pet.articles.model.dto.payload.AuthRequest
import pet.articles.model.dto.payload.NewArticlePayload
import pet.articles.model.dto.payload.RegistrationPayload
import pet.articles.model.dto.payload.ReviewPayload
import pet.articles.model.dto.payload.UpdateArticlePayload
import pet.articles.model.dto.payload.UserPayload

fun User.toRegistrationPayload(): RegistrationPayload = RegistrationPayload(
    username = username,
    email = email,
    password = password!!
)

fun User.toUserPayload(): UserPayload = UserPayload(
    username = username,
    email = email,
    role = role,
    password = password!!
)

fun User.toAuthRequest(): AuthRequest = AuthRequest(
    username = username,
    password = password!!
)

fun Article.toNewArticlePayload(authorsIds: List<Int>): NewArticlePayload = NewArticlePayload(
    topic = topic,
    content = content,
    authorIds = authorsIds
)

fun Article.toUpdateArticlePayload(): UpdateArticlePayload = UpdateArticlePayload(
    topic = topic,
    content = content
)

fun Review.toReviewPayload(): ReviewPayload = ReviewPayload(
    type = type,
    content = content,
    authorId = authorId!!,
    articleId = articleId
)




