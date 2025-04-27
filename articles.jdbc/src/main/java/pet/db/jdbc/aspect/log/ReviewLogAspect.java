package pet.db.jdbc.aspect.log;

import lombok.extern.slf4j.Slf4j;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;

import org.springframework.stereotype.Component;

import pet.db.jdbc.model.dto.Review;

@Slf4j
@Aspect
@Component
public class ReviewLogAspect {

    @AfterReturning(
            pointcut = "execution(* pet.db.jdbc.service.ReviewServiceImpl.create(..))",
            returning = "createdReview")
    public void logReviewCreation(Review createdReview) {
        log.info("Review was created successfully {}", createdReview);
    }

    @AfterReturning("execution(* pet.db.jdbc.service.ReviewServiceImpl.deleteById(..)) && args(id)")
    public void logReviewDeletion(Integer id) {
        log.info("Review with id = {} was deleted successfully", id);
    }

}
