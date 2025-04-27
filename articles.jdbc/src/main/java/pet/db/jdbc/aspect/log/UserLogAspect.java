package pet.db.jdbc.aspect.log;

import lombok.extern.slf4j.Slf4j;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;

import org.springframework.stereotype.Component;

import pet.db.jdbc.model.dto.User;

@Slf4j
@Aspect
@Component
public class UserLogAspect {

    @AfterReturning(
            pointcut = "execution(* pet.db.jdbc.service.UserServiceImpl.create(..))",
            returning = "createdUser")
    public void logUserCreation(User createdUser) {
        log.info("User was created successfully {}", createdUser);
    }

    @AfterReturning(
            pointcut = "execution(* pet.db.jdbc.service.UserServiceImpl.updateById(..))",
            returning = "updatedUser")
    public void logUserUpdate(User updatedUser) {
        log.info("User with id = {} was updated successfully {}", updatedUser.getId(), updatedUser);
    }

    @AfterReturning("execution(* pet.db.jdbc.service.UserServiceImpl.deleteById(..)) && args(id)")
    public void logUserDeletion(Integer id) {
        log.info("User with id = {} was deleted successfully", id);
    }

}
