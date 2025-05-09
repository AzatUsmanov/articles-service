package pet.db.jdbc.tool.generator;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import pet.db.jdbc.model.dto.payload.RegistrationPayload;
import pet.db.jdbc.model.dto.User;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RegistrationPayloadTestDataGenerator implements TestDataGenerator<RegistrationPayload> {

    private final TestDataGenerator<User> userTestDataGenerator;

    @Override
    public RegistrationPayload generateSavedData() {
        return generateSavedData(1).getFirst();
    }

    @Override
    public RegistrationPayload generateUnsavedData() {
        return generateUnsavedData(1).getFirst();
    }

    @Override
    public List<RegistrationPayload> generateSavedData(Integer dataSize) {
        return userTestDataGenerator.generateSavedData(dataSize).stream()
                .map(this::convertToRegistrationPayload)
                .toList();
    }

    @Override
    public List<RegistrationPayload> generateUnsavedData(Integer dataSize) {
        return userTestDataGenerator.generateUnsavedData(dataSize).stream()
                .map(this::convertToRegistrationPayload)
                .toList();
    }

    private RegistrationPayload convertToRegistrationPayload(User user) {
        return new RegistrationPayload(
                user.getUsername(),
                user.getEmail(),
                user.getPassword());
    }

}
