package pet.db.jdbc.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import pet.db.jdbc.model.enums.UserRole;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private Integer id;

    private String username;

    private String email;

    private UserRole role;

    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private String password;

}
