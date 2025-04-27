package pet.db.jdbc.service;

import jakarta.validation.constraints.NotNull;

import pet.db.jdbc.model.dto.User;
import pet.db.jdbc.tool.exception.DuplicateUserException;

public interface RegistrationService {

    User register(@NotNull User user) throws DuplicateUserException;

}
