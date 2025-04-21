package pet.db.jdbc.service;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.stereotype.Service;

import pet.db.jdbc.entity.User;

import java.util.NoSuchElementException;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserPermissionServiceImpl implements UserPermissionService {

    private final UserService userService;

    @Override
    public boolean checkUserForEditPermissionById(Integer userId) {
        GrantedAuthority userRoleAuthority = new SimpleGrantedAuthority(User.Role.ROLE_USER.toString());
        UserDetails currentUser = getCurrentUser();

        if (currentUser.getAuthorities().contains(userRoleAuthority)) {
            User targetUser = getTargetUserById(userId);
            return isCurrentUserMatchesTarget(currentUser, targetUser);
        }
        return true;
    }

    private User getTargetUserById(Integer userId) {
        return userService.findById(userId).orElseThrow(NoSuchElementException::new);
    }

    private UserDetails getCurrentUser() {
        return (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private boolean isCurrentUserMatchesTarget(UserDetails currentUser, User targetUser) {
        return Objects.equals(currentUser.getUsername(), targetUser.getUsername());
    }

}
