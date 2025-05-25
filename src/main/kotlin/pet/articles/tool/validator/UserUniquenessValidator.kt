package pet.articles.tool.validator

import pet.articles.model.dto.User
import pet.articles.service.user.UserExistenceChecker
import pet.articles.tool.exception.DuplicateUserException

class UserUniquenessValidator(
    private val userExistenceChecker: UserExistenceChecker
) : UniquenessValidator<User> {

    override fun validate(item: User) {
        validateUsernameUniqueness(item.username)
        validateEmailUniqueness(item.email)
    }

    override fun validate(item: User, itemForComparison: User) {
        if (item.username != itemForComparison.username) {
            validateUsernameUniqueness(item.username)
        }
        if (item.email != itemForComparison.email) {
            validateEmailUniqueness(item.email)
        }
    }

    private fun validateUsernameUniqueness(username: String) {
        if (userExistenceChecker.existsByUsername(username)) {
            throw DuplicateUserException("User with username $username already exists")
        }
    }

    private fun validateEmailUniqueness(email: String) {
        if (userExistenceChecker.existsByEmail(email)) {
            throw DuplicateUserException("User with email $email already exists")
        }
    }
}