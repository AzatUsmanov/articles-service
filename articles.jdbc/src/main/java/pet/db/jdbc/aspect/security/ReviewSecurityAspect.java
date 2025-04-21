package pet.db.jdbc.aspect.security;

import lombok.RequiredArgsConstructor;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import pet.db.jdbc.controller.payload.ReviewPayload;
import pet.db.jdbc.service.ReviewService;
import pet.db.jdbc.service.UserPermissionService;

@Aspect
@Component
@RequiredArgsConstructor
public class ReviewSecurityAspect {

    private final UserPermissionService userPermissionService;

    private final ReviewService reviewService;

    @Before("execution(* pet.db.jdbc.controller.ReviewController.create(..)) && args(reviewPayload)")
    public void secureReviewCreation(ReviewPayload reviewPayload) {
        secureEditMethod(reviewPayload.authorId(), "update");
    }

    @Before("execution(* pet.db.jdbc.controller.ReviewController.deleteById(..)) && args(id)")
    public void secureReviewDeletion(Integer id) {
        reviewService.findById(id)
                .ifPresent(review -> secureEditMethod(review.getAuthorId(), "delete"));
    }

    private void secureEditMethod(Integer userId, String editMethodName) {
        if (!userPermissionService.checkUserForEditPermissionById(userId)) {
            throw new AccessDeniedException(
                    String.format("Attempt to %s user without proper permission", editMethodName));
        }
    }

}
