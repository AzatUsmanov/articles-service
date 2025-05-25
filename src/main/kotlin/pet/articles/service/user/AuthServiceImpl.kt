package pet.articles.service.user

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import org.mindrot.jbcrypt.BCrypt
import pet.articles.model.dto.User
import pet.articles.model.dto.payload.AuthRequest
import pet.articles.model.dto.AuthResponse
import pet.articles.model.enums.AuthClaim
import pet.articles.tool.exception.BadCredentialsException
import pet.articles.tool.exception.UsernameNotFoundException
import java.util.Date

class AuthServiceImpl(
    private val userService: UserService,
    private val secret: String,
    private val issuer: String,
    private val audience: String,
    private val expiresIn: String
): AuthService {

    override fun authenticate(request: AuthRequest): AuthResponse {
        val user: User = userService.findByUsername(request.username)
            ?: throw UsernameNotFoundException("User with username = ${request.username} not found")

        return if (!BCrypt.checkpw(request.password, user.password)) {
            throw BadCredentialsException("Invalid password")
        } else AuthResponse(generateToken(user))
    }

    private fun generateToken(user: User): String {
        val expiresAt = Date(System.currentTimeMillis() + expiresIn.toInt())
        return JWT.create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withClaim(AuthClaim.USERNAME.toString(), user.username)
            .withClaim(AuthClaim.ID.toString(), user.id!!.toString())
            .withClaim(AuthClaim.ROLE.toString(), user.role.name)
            .withExpiresAt(expiresAt)
            .sign(Algorithm.HMAC256(secret))
    }
}