package pet.articles.tool.validator

import io.ktor.server.plugins.requestvalidation.*

interface UniquenessValidator<T> : Validator<T> {

    fun validate(item: T, itemForComparison: T)
}