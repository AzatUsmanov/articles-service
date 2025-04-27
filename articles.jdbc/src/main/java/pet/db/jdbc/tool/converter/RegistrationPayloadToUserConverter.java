package pet.db.jdbc.tool.converter;


import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import pet.db.jdbc.model.dto.User;
import pet.db.jdbc.model.dto.payload.RegistrationPayload;
import pet.db.jdbc.model.enums.UserRole;

@Component
public class RegistrationPayloadToUserConverter implements Converter<RegistrationPayload, User> {

        @Override
        public User convert(RegistrationPayload source) {
                return User.builder()
                        .username(source.username())
                        .email(source.email())
                        .password(source.password())
                        .role(UserRole.ROLE_USER)
                        .build();
        }

}
