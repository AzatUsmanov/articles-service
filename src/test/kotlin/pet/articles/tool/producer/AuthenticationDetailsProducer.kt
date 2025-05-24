package pet.articles.tool.producer


import pet.articles.model.dto.User
import pet.articles.model.enums.UserRole

interface AuthenticationDetailsProducer {

    fun produceRegisteredUserWithRawPassword(role: UserRole = UserRole.ROLE_USER): User

    fun produceRegisteredUser(role: UserRole = UserRole.ROLE_USER): User
}