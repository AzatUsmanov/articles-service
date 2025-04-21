package pet.db.jdbc.util;

import java.util.List;
import java.util.stream.Collectors;

public class SqlUtils {

    public static String buildInClause(String sqlTemplate, List<Integer> ids) {
        String placeholders = ids
                .stream()
                .map(Object::toString)
                .collect(Collectors.joining(","));
        return String.format(sqlTemplate, placeholders);
    }

}
