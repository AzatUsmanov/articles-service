package pet.articles.test.tool.producer


import pet.articles.model.dto.User
import pet.articles.model.enums.UserRole
import pet.articles.service.RegistrationService
import pet.articles.test.tool.generator.TestDataGenerator

class AuthenticationDetailsProducerImpl(
    private val userTestDataGenerator: TestDataGenerator<User>,
    private val registrationService: RegistrationService
) : AuthenticationDetailsProducer {

    override fun produceRegisteredUserWithRawPassword(role: UserRole): User {
        val unsavedUser: User = userTestDataGenerator.generateUnsavedData().copy(
            role = role
        )
        return registrationService.register(unsavedUser).copy(
            password = unsavedUser.password
        )
    }

    override fun produceRegisteredUser(role: UserRole): User {
        val unsavedUser: User = userTestDataGenerator.generateUnsavedData().copy(
            role = role
        )
        return registrationService.register(unsavedUser)
    }
}