package pet.db.jdbc.model.enums;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum AuthorshipOfArticleColumn {

    AUTHOR_ID("author_id"),
    ARTICLE_ID("article_id");

    private final String name;

    @Override
    public String toString() {
        return name;
    }

}
