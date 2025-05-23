package pet.articles.test.tool.extension

import pet.articles.model.dto.Article
import pet.articles.model.dto.Review
import pet.articles.model.dto.User
import pet.articles.model.dto.payload.NewArticlePayload
import pet.articles.model.dto.payload.RegistrationPayload
import pet.articles.model.dto.payload.ReviewPayload
import pet.articles.model.dto.payload.UpdateArticlePayload
import pet.articles.model.dto.payload.UserPayload

fun User.isMatches(userPayload: UserPayload): Boolean =
    username == userPayload.username &&
            email == userPayload.email &&
            role == userPayload.role

fun User.isMatches(registrationPayload: RegistrationPayload): Boolean =
    username == registrationPayload.username &&
            email == registrationPayload.email

fun Article.isMatches(newArticlePayload: NewArticlePayload): Boolean =
    topic == newArticlePayload.topic &&
            content == newArticlePayload.content

fun Article.isMatches(updateArticlePayload: UpdateArticlePayload): Boolean =
    topic == updateArticlePayload.topic &&
            content == updateArticlePayload.content

fun Review.isMatches(reviewPayload: ReviewPayload): Boolean =
    type == reviewPayload.type &&
        content == reviewPayload.content &&
        authorId == reviewPayload.authorId &&
        articleId == reviewPayload.articleId
