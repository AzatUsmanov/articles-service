package pet.articles.tool.producer


import pet.articles.model.dto.User
import pet.articles.model.enums.UserRole
import pet.articles.service.RegistrationService
import pet.articles.tool.generator.TestDataGenerator

class AuthenticationDetailsProducerImpl(
    private val userGenerator: TestDataGenerator<User>,
    private val registrationService: RegistrationService
) : AuthenticationDetailsProducer {

    override fun produceRegisteredUserWithRawPassword(role: UserRole): User {
        val unsavedUser: User = userGenerator
            .generateUnsavedData()
            .copy(role = role)
        return registrationService
            .register(unsavedUser)
            .copy(password = unsavedUser.password)
    }

    override fun produceRegisteredUser(role: UserRole): User =
        userGenerator
            .generateUnsavedData()
            .copy(role = role)
            .let(registrationService::register)

}