package pet.articles.service.user

import pet.articles.model.dto.payload.AuthRequest
import pet.articles.model.dto.AuthResponse

interface AuthService {

    fun authenticate(request: AuthRequest): AuthResponse
}