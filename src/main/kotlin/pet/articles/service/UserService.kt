package pet.articles.service

import pet.articles.model.dto.User

interface UserService {

    fun create(user: User): User

    fun updateById(user: User, id: Int): User

    fun deleteById(id: Int)

    fun findById(id: Int): User?

    fun findByUsername(username: String): User?

    fun findByIds(userIds: List<Int>): List<User>

    fun findAll(): List<User>

    fun findAuthorsByArticleId(articleId: Int): List<User>

    fun findAuthorIdsByArticleId(articleId: Int): List<Int>

    fun existsById(id: Int): Boolean

    fun existsByUsername(username: String): Boolean

    fun existsByEmail(email: String): Boolean
}