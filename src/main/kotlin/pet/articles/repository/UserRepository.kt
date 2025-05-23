package pet.articles.repository


import pet.articles.model.dto.User


interface UserRepository {

    fun save(user: User): User

    fun updateById(user: User, id: Int): User

    fun deleteById(id: Int)

    fun findById(id: Int): User?

    fun findByUsername(username: String): User?

    fun findByEmail(email: String): User?

    fun findByIds(ids: List<Int>): List<User>

    fun findAll(): List<User>

    fun existsById(id: Int): Boolean = findById(id) != null

    fun existsByUsername(username: String): Boolean = findByUsername(username) != null

    fun existsByEmail(email: String): Boolean = findByEmail(email) != null
}