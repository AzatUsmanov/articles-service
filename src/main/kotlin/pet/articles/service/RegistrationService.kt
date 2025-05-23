package pet.articles.service

import pet.articles.model.dto.User

interface RegistrationService {

    fun register(user: User): User
}

