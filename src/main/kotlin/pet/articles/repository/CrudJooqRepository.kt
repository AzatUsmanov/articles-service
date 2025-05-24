package pet.articles.repository

import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.RecordMapper
import org.jooq.RecordUnmapper
import org.jooq.Table
import org.jooq.TableField
import org.jooq.TableRecord
import org.jooq.exception.DataAccessException
import pet.articles.tool.extension.toUnit

open class CrudJooqRepository<T, R : TableRecord<R>>(
    private val dsl: DSLContext,
    private val table: Table<R>,
    private val recordMapper: RecordMapper<Record, T>,
    private val recordUnmapper: RecordUnmapper<T, R>,
    private val idField: TableField<R, Int?>
) : CrudRepository<T> {

    override fun save(item: T): T =
        dsl.transactionResult { config ->
            config.dsl()
                .insertInto(table)
                .set(recordUnmapper.unmap(item))
                .returning()
                .fetchOne()
                ?.map(recordMapper)
                ?: throw DataAccessException("Failed to save $item")
        }

    override fun updateById(item: T, id: Int): T =
        dsl.transactionResult { config ->
            config.dsl()
                .update(table)
                .set(recordUnmapper.unmap(item))
                .where(idField.eq(id))
                .returning()
                .fetchOne()
                ?.map(recordMapper)
                ?: throw DataAccessException("$item with id = $id was not updated")
        }

    override fun deleteById(id: Int) =
        dsl.deleteFrom(table)
            .where(idField.eq(id))
            .execute()
            .toUnit()

    override fun findById(id: Int): T? =
        dsl.selectFrom(table)
            .where(idField.eq(id))
            .fetchOne()
            ?.map(recordMapper)

    override fun findByIds(ids: List<Int>): List<T> =
        dsl.selectFrom(table)
            .where(idField.`in`(ids))
            .fetch()
            .map(recordMapper)

    override fun findAll(): List<T> =
        dsl.selectFrom(table)
            .fetch()
            .map(recordMapper)
}