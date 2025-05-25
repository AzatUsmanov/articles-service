package pet.articles.service.user

import pet.articles.repository.user.UserRepository

class UserExistenceCheckerImpl(
    private val userRepository: UserRepository
) : UserExistenceChecker {

    override fun existsByUsername(username: String): Boolean =
        userRepository.existsByUsername(username)

    override fun existsByEmail(email: String): Boolean =
        userRepository.existsByEmail(email)
}