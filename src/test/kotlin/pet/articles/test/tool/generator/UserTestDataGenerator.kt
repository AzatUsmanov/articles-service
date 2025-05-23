package pet.articles.test.tool.generator

import net.datafaker.Faker
import org.koin.core.annotation.Named

import org.koin.core.annotation.Single

import pet.articles.model.dto.User
import pet.articles.model.enums.UserRole
import pet.articles.service.UserService
import pet.articles.test.tool.extension.generateRandom
import pet.articles.web.validation.UserPayloadValidation.Fields.Constrains.EMAIL_MAX_LENGTH
import pet.articles.web.validation.UserPayloadValidation.Fields.Constrains.PASSWORD_MAX_LENGTH
import pet.articles.web.validation.UserPayloadValidation.Fields.Constrains.PASSWORD_MIN_LENGTH
import pet.articles.web.validation.UserPayloadValidation.Fields.Constrains.USERNAME_MAX_LENGTH

class UserTestDataGenerator(
    private val userService: UserService,
    private val faker: Faker = Faker()
) : TestDataGenerator<User> {

    companion object {
        const val USER_FIELD_USERNAME_INVALID_LENGTH= 1000
    }

    override fun generateUnsavedData(dataSize: Int): List<User> =
        (1..dataSize).map {
            User(
                id = faker.number().positive(),
                username = faker.name().fullName().take(USERNAME_MAX_LENGTH),
                email = faker.internet().emailAddress().take(EMAIL_MAX_LENGTH),
                role = UserRole.entries.random(),
                password = faker.internet().password(PASSWORD_MIN_LENGTH, PASSWORD_MAX_LENGTH)
            )
        }

    override fun generateSavedData(dataSize: Int): List<User> {
        val a = generateUnsavedData(dataSize)
            return a.map(userService::create)
    }

    override fun generateInvalidData(): User = generateUnsavedData().copy(
        username = String.generateRandom(USER_FIELD_USERNAME_INVALID_LENGTH)
    )
}