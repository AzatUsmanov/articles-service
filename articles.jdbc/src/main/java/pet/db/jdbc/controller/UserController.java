package pet.db.jdbc.controller;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.core.convert.converter.Converter;
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

import pet.db.jdbc.model.dto.payload.UserPayload;
import pet.db.jdbc.model.dto.User;
import pet.db.jdbc.service.UserService;
import pet.db.jdbc.tool.exception.DuplicateUserException;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "${api.paths.users}",
        produces= MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE)
public class UserController {

    private final UserService userService;

    private final Converter<UserPayload, User> userPayloadToUserConverter;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User create(@RequestBody @Valid UserPayload userPayload) throws DuplicateUserException {
        User user = userPayloadToUserConverter.convert(userPayload);
        return userService.create(user);
    }

    @PatchMapping("/{id}")
    public User updateById(@RequestBody @Valid UserPayload userPayload, @PathVariable("id") Integer id) throws DuplicateUserException {
        User user = userPayloadToUserConverter.convert(userPayload);
        return userService.updateById(user, id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(@PathVariable("id") Integer id) {
        userService.deleteById(id);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> findById(@PathVariable("id") Integer id) {
        Optional<User> userOptional = userService.findById(id);
        return userOptional
                .map(user -> new ResponseEntity<>(user, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/authorship/{articleId}")
    List<User> findAuthorsOfArticle(@PathVariable("articleId") Integer articleId) {
        return userService.findAuthorsByArticleId(articleId);
    }

    @GetMapping
    public List<User> getAll() {
        return userService.findAll();
    }

}
