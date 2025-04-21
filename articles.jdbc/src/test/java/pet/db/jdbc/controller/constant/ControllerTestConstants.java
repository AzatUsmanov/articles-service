package pet.db.jdbc.controller.constant;

public final class ControllerTestConstants {

    public static class Fields {

        public static final String USERNAME = "username";

        public static final String EMAIL = "email";

    }

    public static class JsonPaths {

        public static final String PATH = "$";

        public static final String FIELD = "$.field";

        public static final String ID = "$.id";

        public static final String DATE_OF_CREATION = "$.dateOfCreation";

        public static final String ERROR = "$.error";

        public static final String FIELD_ERRORS = "$.field_errors";

        public static final String LENGTH = "$.length()";

    }

    public static class ErrorMessages {

        public static final String VALIDATION_FIELD = "validation_field";

        public static final String DUPLICATE_FIELD = "duplicate_field";

    }

}
