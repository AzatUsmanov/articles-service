package pet.db.jdbc.tool.generator;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import pet.db.jdbc.model.dto.payload.NewArticlePayload;
import pet.db.jdbc.model.dto.Article;
import pet.db.jdbc.model.dto.User;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class NewArticlePayloadTestDataGenerator implements TestDataGenerator<NewArticlePayload> {

    private final TestDataGenerator<Article> articleTestDataGenerator;;

    private final TestDataGenerator<User> userTestDataGenerator;;

    @Override
    public NewArticlePayload generateSavedData() {
        return generateSavedData(1).getFirst();
    }

    @Override
    public NewArticlePayload generateUnsavedData() {
        return generateUnsavedData(1).getFirst();
    }

    @Override
    public List<NewArticlePayload> generateSavedData(Integer dataSize) {
        return articleTestDataGenerator.generateSavedData(dataSize).stream()
                .map(x -> convertToNewArticlePayload(x, new ArrayList<>(generateSavedAuthorIds(dataSize))))
                .toList();
    }

    @Override
    public List<NewArticlePayload> generateUnsavedData(Integer dataSize) {
        return articleTestDataGenerator.generateUnsavedData(dataSize).stream()
                .map(x -> convertToNewArticlePayload(x, new ArrayList<>(generateSavedAuthorIds(dataSize))))
                .toList();
    }

    private NewArticlePayload convertToNewArticlePayload(Article article, List<Integer> savedAuthorIds) {
        return new NewArticlePayload(
                article.getTopic(),
                article.getContent(),
                savedAuthorIds);
    }

    private List<Integer> generateSavedAuthorIds(Integer dataSize) {
        return userTestDataGenerator.generateSavedData(dataSize).stream()
                .map(User::getId)
                .toList();
    }

}
