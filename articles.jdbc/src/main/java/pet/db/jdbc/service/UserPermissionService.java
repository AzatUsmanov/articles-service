package pet.db.jdbc.service;

import jakarta.validation.constraints.NotNull;

public interface UserPermissionService {

    boolean checkUserForEditPermissionById(@NotNull Integer userId);

}
