package pet.articles.service.user

import pet.articles.model.dto.User
import pet.articles.service.CrudService

interface UserService : CrudService<User>, UserExistenceChecker {

    fun findByUsername(username: String): User?

    fun findAuthorsByArticleId(articleId: Int): List<User>

    fun findAuthorIdsByArticleId(articleId: Int): List<Int>
}