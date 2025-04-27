package pet.db.jdbc.tool.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import pet.db.jdbc.model.dto.User;
import pet.db.jdbc.model.dto.payload.UserPayload;

@Component
public class UserPayloadToUserConverter implements Converter<UserPayload, User> {

        @Override
        public User convert(UserPayload source) {
                return User.builder()
                        .username(source.username())
                        .email(source.email())
                        .password(source.password())
                        .role(source.role())
                        .build();
        }

}
