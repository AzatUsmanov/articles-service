package pet.articles.test.tool.extension

fun String.Companion.generateRandom(size: Int): String =
    generateSequence { "abc".random() }
        .take(size)
        .joinToString("")
