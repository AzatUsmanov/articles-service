package pet.db.jdbc.tool.generator;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import pet.db.jdbc.model.dto.payload.UpdateArticlePayload;
import pet.db.jdbc.model.dto.Article;

import java.util.List;

@Component
@RequiredArgsConstructor
public class UpdateArticlePayloadTestDataGenerator implements TestDataGenerator<UpdateArticlePayload> {

    private final TestDataGenerator<Article> articleTestDataGenerator;;

    @Override
    public UpdateArticlePayload generateSavedData() {
        return generateSavedData(1).getFirst();
    }

    @Override
    public UpdateArticlePayload generateUnsavedData() {
        return generateUnsavedData(1).getFirst();
    }

    @Override
    public List<UpdateArticlePayload> generateSavedData(Integer dataSize) {
        return articleTestDataGenerator.generateSavedData(dataSize).stream()
                .map(this::convertToUpdateArticlePayload)
                .toList();
    }

    @Override
    public List<UpdateArticlePayload> generateUnsavedData(Integer dataSize) {
        return articleTestDataGenerator.generateUnsavedData(dataSize).stream()
                .map(this::convertToUpdateArticlePayload)
                .toList();
    }

    private UpdateArticlePayload convertToUpdateArticlePayload(Article article) {
        return new UpdateArticlePayload(
                article.getTopic(),
                article.getContent());
    }

}
