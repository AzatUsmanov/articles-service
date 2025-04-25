package pet.db.jdbc.controller;


import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import pet.db.jdbc.controller.payload.NewArticlePayload;
import pet.db.jdbc.controller.payload.UpdateArticlePayload;
import pet.db.jdbc.entity.Article;
import pet.db.jdbc.service.ArticleService;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "${api.paths.articles}",
        produces= MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE)
public class ArticleController {

    private final ArticleService articleService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Article create(@RequestBody @Valid NewArticlePayload articlePayload) {
        return articleService.create(new Article(articlePayload), articlePayload.authorIds());
    }

    @PatchMapping("/{id}")
    public Article updateById(@RequestBody @Valid UpdateArticlePayload articlePayload, @PathVariable("id") Integer id) {
        return articleService.updateById(new Article(articlePayload), id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(@PathVariable("id") Integer id) {
        articleService.deleteById(id);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Article> getById(@PathVariable("id") Integer id) {
        Optional<Article> articleOptional = articleService.findById(id);
        return articleOptional
                .map(user -> new ResponseEntity<>(user, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/authorship/{authorId}")
    List<Article> findAuthorsOfArticle(@PathVariable("authorId") Integer authorId) {
        return articleService.findArticlesByAuthorId(authorId);
    }

    @GetMapping
    public List<Article> getAll() {
        return articleService.findAll();
    }

}
