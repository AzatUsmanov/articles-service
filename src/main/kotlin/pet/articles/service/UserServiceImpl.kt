package pet.articles.service

import org.koin.core.annotation.Single

import pet.articles.model.dto.User
import pet.articles.repository.AuthorshipOfArticleRepository
import pet.articles.repository.UserRepository
import pet.articles.tool.exception.DuplicateUserException

@Single
class UserServiceImpl(
    private val userRepository: UserRepository,
    private val authorshipOfArticleRepository: AuthorshipOfArticleRepository
) : UserService {

    override fun create(user: User): User {
        validateUserUniqueness(user, null)
        return userRepository.save(user)
    }

    override fun updateById(user: User, id: Int): User {
        val targetUser: User = userRepository.findById(id)
            ?: throw NoSuchElementException("User with id $id not found")
        validateUserUniqueness(user, targetUser)
        return userRepository.updateById(user, id)
    }

    override fun deleteById(id: Int) = userRepository.deleteById(id)

    override fun findById(id: Int): User? = userRepository.findById(id)

    override fun findByUsername(username: String): User? = userRepository.findByUsername(username)

    override fun findByIds(userIds: List<Int>): List<User> =
        if (userIds.isNotEmpty()) userRepository.findByIds(userIds) else emptyList()

    override fun findAll(): List<User> = userRepository.findAll()

    override fun findAuthorIdsByArticleId(articleId: Int): List<Int> =
        findAuthorsByArticleId(articleId).mapNotNull(User::id)

    override fun findAuthorsByArticleId(articleId: Int): List<User> {
        val authorsIds: List<Int> = authorshipOfArticleRepository.findAuthorIdsByArticleId(articleId)
        return findByIds(authorsIds)
    }

    override fun existsById(id: Int): Boolean = userRepository.existsById(id)

    override fun existsByUsername(username: String): Boolean = userRepository.existsByUsername(username)

    override fun existsByEmail(email: String): Boolean = userRepository.existsByEmail(email)

    private fun validateUserUniqueness(user: User, existingUser: User?) {
        validateUsernameUniqueness(user, existingUser)
        validateUserEmailUniqueness(user, existingUser)
    }

    private fun validateUsernameUniqueness(user: User, existingUser: User?) {
        val isValid = !existsByUsername(user.username) ||
                existingUser?.let { it.username == user.username } ?: false
        if (!isValid) {
            throw DuplicateUserException("User with username ${user.username} already exists")
        }
    }

    private fun validateUserEmailUniqueness(user: User, existingUser: User?) {
        val isValid = !existsByEmail(user.email) ||
                existingUser?.let { it.email == user.email } ?: false
        if (!isValid) {
            throw DuplicateUserException("User with email ${user.email} already exists")
        }
    }
}