package pet.db.jdbc.tool.generator;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public interface TestDataGenerator<T> {

    T generateSavedData();

    T generateUnsavedData();

    List<T> generateSavedData(@NotNull Integer dataSize);

    List<T> generateUnsavedData(@NotNull Integer dataSize);

}
