package pet.articles.service

interface UserPermissionService {

    fun checkCurrentUserForEditPermissionById(id: Int): Boolean

    fun checkCurrentUserIsAdmin(): Boolean
}