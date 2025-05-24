package pet.articles.tool.generator

import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

import pet.articles.model.dto.User
import pet.articles.model.dto.payload.UserPayload

class UserPayloadTestDataGenerator(
    private val userGenerator: TestDataGenerator<User>
) : TestDataGenerator<UserPayload> {

    override fun generateUnsavedData(dataSize: Int): List<UserPayload> =
        userGenerator.generateUnsavedData(dataSize).map(::convertToUserPayload)

    override fun generateSavedData(dataSize: Int): List<UserPayload> =
        userGenerator.generateSavedData(dataSize).map(::convertToUserPayload)

    override fun generateInvalidData(): UserPayload =
        convertToUserPayload(userGenerator.generateInvalidData())

    private fun convertToUserPayload(user: User): UserPayload =
        UserPayload(
            username = user.username,
            email = user.email,
            password = user.password!!,
            role = user.role
        )
}