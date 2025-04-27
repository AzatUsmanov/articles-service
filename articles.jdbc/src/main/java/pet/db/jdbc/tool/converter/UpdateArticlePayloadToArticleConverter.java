package pet.db.jdbc.tool.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import pet.db.jdbc.model.dto.Article;
import pet.db.jdbc.model.dto.payload.UpdateArticlePayload;

@Component
public class UpdateArticlePayloadToArticleConverter implements Converter<UpdateArticlePayload, Article> {

        @Override
        public Article convert(UpdateArticlePayload source) {
                return Article.builder()
                        .topic(source.topic())
                        .content(source.content())
                        .build();
        }

}
