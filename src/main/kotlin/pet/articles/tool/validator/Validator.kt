package pet.articles.tool.validator

interface Validator<T> {

    fun validate(item: T)
}