package pet.db.jdbc.aspect.security;

import lombok.RequiredArgsConstructor;

import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Aspect;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import pet.db.jdbc.controller.payload.UserPayload;
import pet.db.jdbc.service.UserPermissionService;

@Aspect
@Component
@RequiredArgsConstructor
public class UserSecurityAspect {

    private final UserPermissionService userPermissionService;

    @Before("execution(* pet.db.jdbc.controller.UserController.updateById(..)) && args(userPayload, id)")
    public void secureUserUpdate(UserPayload userPayload, Integer id) {
        secureEditMethod(id, "update");
    }

    @Before("execution(* pet.db.jdbc.controller.UserController.deleteById(..)) && args(id)")
    public void secureUserDeletion(Integer id) {
        secureEditMethod(id, "delete");
    }

    private void secureEditMethod(Integer userId, String editMethodName) {
        if (!userPermissionService.checkUserForEditPermissionById(userId)) {
            throw new AccessDeniedException(
                    String.format("Attempt to %s user without proper permission", editMethodName));
        }
    }

}
