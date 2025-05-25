package pet.articles.service.user

import org.koin.core.logger.Logger

import pet.articles.model.dto.User
import pet.articles.repository.authorship.AuthorshipOfArticleRepository
import pet.articles.repository.user.UserRepository
import pet.articles.service.CrudServiceBaseImpl
import pet.articles.tool.validator.UniquenessValidator

class UserServiceImpl(
    private val userRepository: UserRepository,
    private val authorshipRepository: AuthorshipOfArticleRepository,
    private val userExistenceChecker: UserExistenceChecker,
    private val userUniquenessValidator: UniquenessValidator<User>,
    log: Logger
) : UserService,
    UserExistenceChecker by userExistenceChecker,
    CrudServiceBaseImpl<User>(
        crudRepository = userRepository,
        log = log
    ) {
    override fun create(item: User): User {
        userUniquenessValidator.validate(item)
        return super.create(item)
    }

    override fun updateById(item: User, id: Int): User {
        val targetUser: User = userRepository.findById(id)
            ?: throw NoSuchElementException("User with id $id not found")
        userUniquenessValidator.validate(item, targetUser)
        return super.updateById(item, id)
    }

    override fun findByUsername(username: String): User? =
        userRepository.findByUsername(username)

    override fun findAuthorIdsByArticleId(articleId: Int): List<Int> =
        findAuthorsByArticleId(articleId).mapNotNull(User::id)

    override fun findAuthorsByArticleId(articleId: Int): List<User> {
        val authorsIds: List<Int> = authorshipRepository.findAuthorIdsByArticleId(articleId)
        return findByIds(authorsIds)
    }
}