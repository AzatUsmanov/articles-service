package pet.articles.repository


import pet.articles.model.dto.User


interface UserRepository : CrudRepository<User> {

    fun findByUsername(username: String): User?

    fun findByEmail(email: String): User?

    fun existsByUsername(username: String): Boolean = findByUsername(username) != null

    fun existsByEmail(email: String): Boolean = findByEmail(email) != null
}