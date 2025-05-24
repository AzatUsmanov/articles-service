package pet.articles.tool.extension

fun String.Companion.generateRandom(size: Int): String =
    generateSequence { "abc".random() }
        .take(size)
        .joinToString("")
