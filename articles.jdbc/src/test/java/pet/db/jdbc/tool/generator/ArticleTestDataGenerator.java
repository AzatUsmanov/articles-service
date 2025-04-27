package pet.db.jdbc.tool.generator;

import lombok.RequiredArgsConstructor;

import org.instancio.Instancio;
import org.instancio.Select;

import org.springframework.stereotype.Component;

import pet.db.jdbc.model.dto.payload.NewArticlePayload;
import pet.db.jdbc.model.dto.Article;
import pet.db.jdbc.model.dto.User;
import pet.db.jdbc.service.ArticleService;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ArticleTestDataGenerator implements TestDataGenerator<Article> {

    private final TestDataGenerator<User> userTestDataGenerator;

    private final ArticleService articleService;

    @Override
    public Article generateSavedData() {
        return generateSavedData(1).getFirst();
    }

    @Override
    public Article generateUnsavedData() {
        return generateUnsavedData(1).getFirst();
    }

    @Override
    public List<Article> generateSavedData(Integer dataSize) {
        return generateUnsavedData(dataSize).stream()
                .map(article -> articleService.create(article, generateSavedAuthorIds(dataSize)))
                .toList();
    }

    @Override
    public List<Article> generateUnsavedData(Integer dataSize) {
        return Instancio
                .ofList(Article.class)
                .size(dataSize)
                .set(Select.field(Article::getDateOfCreation), LocalDateTime.now())
                .generate(Select.field(Article::getTopic), gen -> gen.string()
                        .length(NewArticlePayload.TOPIC_MIN_LENGTH, NewArticlePayload.TOPIC_MAX_LENGTH)
                        .alphaNumeric())
                .generate(Select.field(Article::getContent), gen -> gen.string()
                        .length(NewArticlePayload.CONTENT_MIN_LENGTH, NewArticlePayload.CONTENT_MAX_LENGTH)
                        .alphaNumeric())
                .create();
    }

    private List<Integer> generateSavedAuthorIds(int dataSize) {
        return userTestDataGenerator.generateSavedData(dataSize)
                .stream()
                .map(User::getId)
                .toList();
    }
}
