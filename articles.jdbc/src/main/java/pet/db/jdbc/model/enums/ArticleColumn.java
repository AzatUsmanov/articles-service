package pet.db.jdbc.model.enums;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ArticleColumn {

    ID("id"),
    DATE_OF_CREATION("date_of_creation"),
    TOPIC("topic"),
    CONTENT("content");

    private final String name;

    @Override
    public String toString() {
        return name;
    }

}
