package pet.articles.repository.user


import pet.articles.model.dto.User
import pet.articles.repository.CrudRepository


interface UserRepository : CrudRepository<User> {

    fun findByUsername(username: String): User?

    fun findByEmail(email: String): User?

    fun existsByUsername(username: String): Boolean = findByUsername(username) != null

    fun existsByEmail(email: String): Boolean = findByEmail(email) != null
}