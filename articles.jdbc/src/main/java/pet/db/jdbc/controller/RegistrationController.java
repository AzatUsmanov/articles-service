package pet.db.jdbc.controller;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import pet.db.jdbc.controller.payload.RegistrationPayload;
import pet.db.jdbc.entity.User;
import pet.db.jdbc.service.RegistrationService;
import pet.db.jdbc.tool.exception.DuplicateUserException;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "${api.paths.registration}",
        produces= MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE)
public class RegistrationController {

    private final RegistrationService registrationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    User register(@RequestBody @Valid RegistrationPayload registrationPayload) throws DuplicateUserException {
        return registrationService.register(new User(registrationPayload));
    }

}
