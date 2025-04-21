package pet.db.jdbc.aspect.log;

import lombok.extern.slf4j.Slf4j;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;

import org.springframework.stereotype.Component;

import pet.db.jdbc.entity.Article;


@Slf4j
@Aspect
@Component
public class ArticleLogAspect {

    @AfterReturning(
            pointcut = "execution(* pet.db.jdbc.service.ArticleServiceImpl.create(..))",
            returning = "createdArticle")
    public void logArticleCreation(Article createdArticle) {
        log.info("Article was created successfully {}", createdArticle);
    }

    @AfterReturning(
            pointcut = "execution(* pet.db.jdbc.service.ArticleServiceImpl.updateById(..))",
            returning = "updatedArticle")
    public void logArticleUpdate(Article updatedArticle) {
        log.info("Article with id = {} was updated successfully {}", updatedArticle.getId(), updatedArticle);
    }

    @AfterReturning("execution(* pet.db.jdbc.service.ArticleServiceImpl.deleteById(..)) && args(id)")
    public void logArticleDeletion(Integer id) {
        log.info("Article with id = {} was deleted successfully", id);
    }

}
