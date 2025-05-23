package pet.articles.util

object SqlUtils {

    fun buildInClause(sqlTemplate: String, values: List<Int>): String {
        val placeholders: String = values.joinToString(",")
        return sqlTemplate.format(placeholders)
    }
}
