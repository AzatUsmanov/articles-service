package pet.articles.tool.generator.payload

import pet.articles.model.dto.User
import pet.articles.model.dto.payload.UserPayload
import pet.articles.tool.generator.TestDataGenerator

class UserPayloadTestDataGenerator(
    userGenerator: TestDataGenerator<User>
) : PayloadTestDataGeneratorBaseImpl<UserPayload, User>(
    convertToPayload = ::convertToUserPayload,
    modelTestDataGenerator = userGenerator
) {

    companion object {
        private fun convertToUserPayload(user: User): UserPayload =
            UserPayload(
                username = user.username,
                email = user.email,
                password = user.password!!,
                role = user.role
            )
    }
}