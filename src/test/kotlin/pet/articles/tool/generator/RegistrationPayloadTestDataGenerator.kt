package pet.articles.tool.generator

import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

import pet.articles.model.dto.User
import pet.articles.model.dto.payload.RegistrationPayload

class RegistrationPayloadTestDataGenerator(
    private val userGenerator: TestDataGenerator<User>
) : TestDataGenerator<RegistrationPayload> {

    override fun generateSavedData(dataSize: Int): List<RegistrationPayload> =
        userGenerator.generateSavedData(dataSize).map(::convertToRegistrationPayload)

    override fun generateUnsavedData(dataSize: Int): List<RegistrationPayload> =
        userGenerator.generateUnsavedData(dataSize).map(::convertToRegistrationPayload)

    override fun generateInvalidData(): RegistrationPayload =
        convertToRegistrationPayload(userGenerator.generateInvalidData())

    private fun convertToRegistrationPayload(user: User): RegistrationPayload =
        RegistrationPayload(
            username = user.username,
            email = user.email,
            password = user.password!!
        )
}