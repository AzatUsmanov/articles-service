package pet.db.jdbc.tool.generator;

import lombok.RequiredArgsConstructor;
import org.instancio.Instancio;
import org.instancio.Select;
import org.springframework.stereotype.Component;
import pet.db.jdbc.controller.payload.UserPayload;
import pet.db.jdbc.entity.User;
import pet.db.jdbc.service.UserService;
import pet.db.jdbc.tool.exception.DuplicateUserException;

import java.util.List;

@Component
@RequiredArgsConstructor
public class UserTestDataGenerator implements TestDataGenerator<User> {

    private final UserService userService;

    @Override
    public User generateSavedData() {
        return generateSavedData(1).getFirst();
    }

    @Override
    public User generateUnsavedData() {
        return generateUnsavedData(1).getFirst();
    }

    @Override
    public List<User> generateSavedData(Integer dataSize) {
        return generateUnsavedData(dataSize).stream()
                .map(user -> {
                    try {
                        return userService.create(user);
                    } catch (DuplicateUserException e) {
                        throw new RuntimeException(e);
                    }
                })
                .toList();
    }

    @Override
    public List<User> generateUnsavedData(Integer dataSize) {
        return Instancio
                .ofList(User.class)
                .size(dataSize)
                .generate(Select.field(User::getUsername), gen -> gen.string()
                        .length(UserPayload.USERNAME_MIN_LENGTH, UserPayload.USERNAME_MAX_LENGTH)
                        .alphaNumeric())
                .generate(Select.field(User::getEmail), gen -> gen.text()
                        .pattern("#a#a#a#a@#a#a#a.com"))
                .generate(Select.field(User::getPassword), gen -> gen.string()
                        .length(UserPayload.PASSWORD_MIN_LENGTH, UserPayload.USERNAME_MAX_LENGTH)
                        .alphaNumeric())
                .create();
    }

}
