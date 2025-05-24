package pet.articles.tool.extension

import org.koin.core.component.KoinComponent

fun KoinComponent.getProperty(key: String): String =
    getKoin().getProperty(key)
        ?: throw NoSuchElementException("Property '$key' is missing")
