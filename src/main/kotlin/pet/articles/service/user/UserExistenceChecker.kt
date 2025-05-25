package pet.articles.service.user

interface UserExistenceChecker {

    fun existsByUsername(username: String): Boolean

    fun existsByEmail(email: String): Boolean
}