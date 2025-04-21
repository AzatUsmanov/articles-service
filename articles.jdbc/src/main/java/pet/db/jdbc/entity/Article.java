package pet.db.jdbc.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import pet.db.jdbc.controller.payload.NewArticlePayload;
import pet.db.jdbc.controller.payload.UpdateArticlePayload;

import java.io.Serializable;
import java.sql.Date;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Article {

    private Integer id;

    private LocalDateTime dateOfCreation;

    private String topic;

    private String content;

    public Article(NewArticlePayload articlePayload) {
        this.dateOfCreation = LocalDateTime.now();
        this.topic = articlePayload.topic();
        this.content = articlePayload.content();
    }

    public Article(UpdateArticlePayload articlePayload) {
        this.topic = articlePayload.topic();
        this.content = articlePayload.content();
    }

}
