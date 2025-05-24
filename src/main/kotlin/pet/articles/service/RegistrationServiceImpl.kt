package pet.articles.service

import org.koin.core.annotation.Single

import org.mindrot.jbcrypt.BCrypt

import pet.articles.model.dto.User

class RegistrationServiceImpl(
    private val userService: UserService,
    private val bcryptSalt: String
) : RegistrationService {

    override fun register(user: User): User {
        val userForRegistration: User = setEncryptedPassword(user)
        return userService.create(userForRegistration)
    }

    private fun setEncryptedPassword(user: User): User =
        user.copy(password = BCrypt.hashpw(user.password, bcryptSalt))
}