package pet.db.jdbc.tool.generator;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import pet.db.jdbc.model.dto.payload.UserPayload;
import pet.db.jdbc.model.dto.User;

import java.util.List;

@Component
@RequiredArgsConstructor
public class UserPayloadTestDataGenerator implements TestDataGenerator<UserPayload> {

    private final TestDataGenerator<User> userTestDataGenerator;

    @Override
    public UserPayload generateSavedData() {
        return generateSavedData(1).getFirst();
    }

    @Override
    public UserPayload generateUnsavedData() {
        return generateUnsavedData(1).getFirst();
    }

    @Override
    public List<UserPayload> generateSavedData(Integer dataSize) {
        return userTestDataGenerator.generateSavedData(dataSize).stream()
                .map(this::convertToUserPayload)
                .toList();
    }

    @Override
    public List<UserPayload> generateUnsavedData(Integer dataSize) {
        return userTestDataGenerator.generateUnsavedData(dataSize).stream()
                .map(this::convertToUserPayload)
                .toList();
    }

    private UserPayload convertToUserPayload(User user) {
        return new UserPayload(
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                user.getRole());
    }

}
