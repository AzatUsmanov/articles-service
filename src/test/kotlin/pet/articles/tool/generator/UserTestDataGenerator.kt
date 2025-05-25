package pet.articles.tool.generator

import net.datafaker.Faker

import pet.articles.model.dto.User
import pet.articles.model.enums.UserRole
import pet.articles.service.user.UserService
import pet.articles.tool.extension.generateRandom
import pet.articles.web.validation.UserPayloadValidation.Fields.Constrains.EMAIL_MAX_LENGTH
import pet.articles.web.validation.UserPayloadValidation.Fields.Constrains.PASSWORD_MAX_LENGTH
import pet.articles.web.validation.UserPayloadValidation.Fields.Constrains.PASSWORD_MIN_LENGTH
import pet.articles.web.validation.UserPayloadValidation.Fields.Constrains.USERNAME_MAX_LENGTH

class UserTestDataGenerator(
    private val userService: UserService
) : TestDataGeneratorBaseImpl<User>(
    generate = ::generate,
    create = userService::create,
    toInvalidState = ::makeInvalid
) {

    companion object {
        private const val USER_FIELD_USERNAME_INVALID_LENGTH= 1000

        private fun generate(faker: Faker = Faker()) =
            User(
                id = faker.number().positive(),
                role = UserRole.entries.random(),
                username = faker
                    .name()
                    .fullName()
                    .take(USERNAME_MAX_LENGTH),
                email = faker
                    .internet()
                    .emailAddress()
                    .take(EMAIL_MAX_LENGTH),
                password = faker
                    .internet()
                    .password(PASSWORD_MIN_LENGTH, PASSWORD_MAX_LENGTH)
            )

        private fun makeInvalid(user: User) =
            user.copy(username = String.generateRandom(USER_FIELD_USERNAME_INVALID_LENGTH))
    }
}