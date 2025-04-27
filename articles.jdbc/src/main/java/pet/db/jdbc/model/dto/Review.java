package pet.db.jdbc.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import pet.db.jdbc.model.enums.ReviewType;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Review {

    private Integer id;

    private ReviewType type;

    private LocalDateTime dateOfCreation;

    private String content;

    private Integer authorId;

    private Integer articleId;

}
