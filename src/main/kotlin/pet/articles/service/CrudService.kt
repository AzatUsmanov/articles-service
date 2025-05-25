package pet.articles.service


interface CrudService<T> {

    fun create(item: T): T

    fun updateById(item: T, id: Int): T

    fun deleteById(id: Int)

    fun findById(id: Int): T?

    fun findByIds(ids: List<Int>): List<T>

    fun findAll(): List<T>

    fun existsById(id: Int): Boolean = findById(id) != null
}