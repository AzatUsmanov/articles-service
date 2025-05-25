package pet.articles.service

import org.koin.core.logger.Logger
import pet.articles.repository.CrudRepository

open class CrudServiceBaseImpl<T>(
    protected val crudRepository: CrudRepository<T>,
    protected val log: Logger
) : CrudService<T> {

    override fun create(item: T): T {
        val savedItem: T = crudRepository.save(item)
        log.info("$savedItem was saved")
        return savedItem
    }

    override fun updateById(item: T, id: Int): T {
        if (!existsById(id)) {
            throw NoSuchElementException("Attempt to update $item by non existent id = $id")
        }
        val updatedItem: T = crudRepository.updateById(item, id)
        log.info("$updatedItem with id = id was updated")
        return updatedItem
    }

    override fun deleteById(id: Int) = crudRepository.deleteById(id)

    override fun findById(id: Int): T? = crudRepository.findById(id)

    override fun findByIds(ids: List<Int>): List<T> = crudRepository.findByIds(ids)

    override fun findAll(): List<T> = crudRepository.findAll()
}