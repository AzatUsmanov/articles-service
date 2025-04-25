package pet.db.jdbc.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
public class AuthorshipOfArticle {

    private Integer articleId;

    private Integer authorId;

    @RequiredArgsConstructor
    public enum Column {

        AUTHOR_ID("author_id"),
        ARTICLE_ID("article_id");

        private final String name;

        @Override
        public String toString() {
            return super.toString();
        }

    }

}
