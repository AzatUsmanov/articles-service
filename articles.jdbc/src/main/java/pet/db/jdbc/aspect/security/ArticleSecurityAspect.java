package pet.db.jdbc.aspect.security;

import lombok.RequiredArgsConstructor;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

import org.springframework.http.HttpMethod;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import pet.db.jdbc.model.dto.payload.NewArticlePayload;
import pet.db.jdbc.model.dto.payload.UpdateArticlePayload;
import pet.db.jdbc.service.UserPermissionService;
import pet.db.jdbc.service.UserService;

import java.util.List;

@Aspect
@Component
@RequiredArgsConstructor
public class ArticleSecurityAspect {

    private final UserPermissionService userPermissionService;

    private final UserService userService;

    @Before("execution(* pet.db.jdbc.controller.ArticleController.create(..)) && args(articlePayload)")
    public void secureArticleCreation(NewArticlePayload articlePayload) {
        secureEditMethod(articlePayload.authorIds(), HttpMethod.POST);
    }

    @Before("execution(* pet.db.jdbc.controller.ArticleController.updateById(..)) && args(articlePayload, id)")
    public void secureArticleUpdate(UpdateArticlePayload articlePayload, Integer id) {
        secureEditMethod(userService.findAuthorIdsByArticleId(id), HttpMethod.PATCH);
    }

    @Before("execution(* pet.db.jdbc.controller.ArticleController.deleteById(..)) && args(id)")
    public void secureArticleDeletion(Integer id) {
        secureEditMethod(userService.findAuthorIdsByArticleId(id), HttpMethod.DELETE);
    }

    private void secureEditMethod(List<Integer> authorIds, HttpMethod method) {
        if (authorIds.isEmpty()) return;

        boolean hasNoEditPermission = authorIds.stream()
                .noneMatch(userPermissionService::checkUserForEditPermissionById);

        if (hasNoEditPermission) {
            throw new AccessDeniedException(
                    "Attempt to %s article without proper permission".formatted(method));
        }
    }

}
