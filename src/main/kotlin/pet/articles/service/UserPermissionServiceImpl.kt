//package pet.articles.service
//
//import org.springframework.security.core.context.SecurityContextHolder
//import org.springframework.security.core.userdetails.UserDetails
//import org.springframework.stereotype.Service
//
//import pet.articles.model.dto.User
//import pet.articles.model.enums.UserRole
//
//@Service
//class UserPermissionServiceImpl(
//    private val userService: UserService
//) : UserPermissionService {
//
//    override fun checkCurrentUserForEditPermissionById(id: Int): Boolean =
//        checkCurrentUserIsAdmin() || isCurrentUserMatchesTarget(id)
//
//    override fun checkCurrentUserIsAdmin(): Boolean =
//        getCurrentUser().authorities.contains(
//            UserRole.ROLE_ADMIN.toGrantedAuthority()
//        )
//
//    private fun getCurrentUser(): UserDetails =
//        SecurityContextHolder.getContext().authentication.principal as UserDetails
//
//    private fun getTargetUserById(id: Int): User = userService.findById(id)
//        ?: throw NoSuchElementException("User with id $id not found")
//
//    private fun isCurrentUserMatchesTarget(targetUserId: Int): Boolean =
//        getCurrentUser().username == getTargetUserById(targetUserId).username
//}