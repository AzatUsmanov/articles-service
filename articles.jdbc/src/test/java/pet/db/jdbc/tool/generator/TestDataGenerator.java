package pet.db.jdbc.tool.generator;

import java.util.List;

public interface TestDataGenerator<T> {

    T generateSavedData();

    T generateUnsavedData();

    List<T> generateSavedData(Integer dataSize);

    List<T> generateUnsavedData(Integer dataSize);

}
