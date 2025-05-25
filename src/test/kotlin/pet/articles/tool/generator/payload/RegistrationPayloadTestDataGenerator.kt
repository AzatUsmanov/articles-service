package pet.articles.tool.generator.payload

import pet.articles.model.dto.User
import pet.articles.model.dto.payload.RegistrationPayload
import pet.articles.tool.generator.TestDataGenerator

class RegistrationPayloadTestDataGenerator(
    userGenerator: TestDataGenerator<User>
) : PayloadTestDataGeneratorBaseImpl<RegistrationPayload, User>(
    convertToPayload = ::convertToRegistrationPayload,
    modelTestDataGenerator = userGenerator
) {

    companion object {
        private fun convertToRegistrationPayload(user: User): RegistrationPayload =
            RegistrationPayload(
                username = user.username,
                email = user.email,
                password = user.password!!
            )
    }
}