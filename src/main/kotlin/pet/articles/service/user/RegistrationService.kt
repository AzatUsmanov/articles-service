package pet.articles.service.user

import pet.articles.model.dto.User

interface RegistrationService {

    fun register(user: User): User
}

