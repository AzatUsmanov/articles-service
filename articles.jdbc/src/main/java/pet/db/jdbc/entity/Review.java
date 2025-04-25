package pet.db.jdbc.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import lombok.RequiredArgsConstructor;
import pet.db.jdbc.controller.payload.ReviewPayload;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.NoSuchElementException;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Review {

    private Integer id;

    private Type type;

    private LocalDateTime dateOfCreation;

    private String content;

    private Integer authorId;

    private Integer articleId;

    public Review(ReviewPayload reviewPayload) {
        this.dateOfCreation = LocalDateTime.now();
        this.type = reviewPayload.type();
        this.content = reviewPayload.content();
        this.authorId = reviewPayload.authorId();
        this.articleId = reviewPayload.articleId();
    }

    @Getter
    public enum Type {

        POSITIVE(0),
        NEUTRAL(1),
        CRITICAL(2);

        private final int number;

        Type(int number) {
            this.number = number;
        }

        public static Type getByNumber(Integer number) {
            return Arrays
                    .stream(Type.values())
                    .filter(x -> x.getNumber() == number).
                    findFirst()
                    .orElseThrow(NoSuchElementException::new);
        }

    }

    @RequiredArgsConstructor
    public enum Column {

        ID("id"),
        TYPE("type"),
        DATE_OF_CREATION("date_of_creation"),
        CONTENT("content"),
        AUTHOR_ID("author_id") ,
        ARTICLE_ID("article_id");

        private final String name;

        @Override
        public String toString() {
            return super.toString();
        }

    }

}
