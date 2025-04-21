package pet.db.jdbc.tool.generator;

import lombok.RequiredArgsConstructor;
import org.instancio.Instancio;
import org.instancio.Select;
import org.springframework.stereotype.Component;
import pet.db.jdbc.controller.payload.NewArticlePayload;
import pet.db.jdbc.controller.payload.UpdateArticlePayload;
import pet.db.jdbc.entity.Article;
import pet.db.jdbc.entity.User;
import pet.db.jdbc.service.ArticleService;

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
