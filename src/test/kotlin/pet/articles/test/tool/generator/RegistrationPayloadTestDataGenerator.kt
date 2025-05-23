package pet.articles.test.tool.generator

import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

import pet.articles.model.dto.User
import pet.articles.model.dto.payload.RegistrationPayload

class RegistrationPayloadTestDataGenerator(
    private val userTestDataGenerator: TestDataGenerator<User>
) : TestDataGenerator<RegistrationPayload> {

    override fun generateSavedData(dataSize: Int): List<RegistrationPayload> =
        userTestDataGenerator.generateSavedData(dataSize).map(::convertToRegistrationPayload)

    override fun generateUnsavedData(dataSize: Int): List<RegistrationPayload> =
        userTestDataGenerator.generateUnsavedData(dataSize).map(::convertToRegistrationPayload)

    override fun generateInvalidData(): RegistrationPayload =
        convertToRegistrationPayload(userTestDataGenerator.generateInvalidData())

    private fun convertToRegistrationPayload(user: User): RegistrationPayload =
        RegistrationPayload(
            username = user.username,
            email = user.email,
            password = user.password!!
        )
}