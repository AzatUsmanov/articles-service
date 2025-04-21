package pet.db.jdbc.service;

import jakarta.validation.constraints.NotNull;
import pet.db.jdbc.entity.User;
import pet.db.jdbc.tool.exception.DuplicateUserException;

import java.util.List;
import java.util.Optional;

public interface RegistrationService {

    User register(@NotNull User user) throws DuplicateUserException;

}
