package pet.db.jdbc.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

}
