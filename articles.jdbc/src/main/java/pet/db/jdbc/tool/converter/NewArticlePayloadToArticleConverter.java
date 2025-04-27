package pet.db.jdbc.tool.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import pet.db.jdbc.model.dto.Article;
import pet.db.jdbc.model.dto.payload.NewArticlePayload;

import java.time.LocalDateTime;

@Component
public class NewArticlePayloadToArticleConverter implements Converter<NewArticlePayload, Article> {

        @Override
        public Article convert(NewArticlePayload source) {
                return Article.builder()
                        .topic(source.topic())
                        .content(source.content())
                        .dateOfCreation(LocalDateTime.now())
                        .build();
        }

}
