package pet.db.jdbc.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthorshipOfArticle {

    private Integer articleId;

    private Integer authorId;

}
