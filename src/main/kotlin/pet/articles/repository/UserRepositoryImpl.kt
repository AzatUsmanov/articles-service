package pet.articles.repository

import org.jooq.DSLContext
import org.jooq.RecordMapper
import org.jooq.RecordUnmapper
import org.jooq.Record
import org.jooq.exception.DataAccessException
import org.koin.core.annotation.Single

import pet.articles.generated.jooq.tables.records.UsersRecord
import pet.articles.generated.jooq.tables.references.USERS
import pet.articles.model.dto.User
import pet.articles.tool.extension.toUnit

class UserRepositoryImpl(
    private val dsl: DSLContext,
    private val userRecordMapper: RecordMapper<Record, User>,
    private val userRecordUnmapper: RecordUnmapper<User, UsersRecord>
) : UserRepository {
    
    override fun save(user: User): User =
        dsl.transactionResult { config ->
            config.dsl()
                .insertInto(USERS)
                .set(userRecordUnmapper.unmap(user))
                .returning()
                .fetchOne()
                ?.map(userRecordMapper)
                ?: throw DataAccessException("user was not saved")
        }
    
    override fun updateById(user: User, id: Int): User =
        dsl.transactionResult { config ->
            config.dsl()
                .update(USERS)
                .set(USERS.USERNAME, user.username)
                .set(USERS.EMAIL, user.email)
                .set(USERS.PASSWORD, user.password)
                .set(USERS.ROLE, user.role.ordinal.toShort())
                .where(USERS.ID.eq(id))
                .returning()
                .fetchOne()
                ?.map(userRecordMapper)
                ?: throw DataAccessException("user with id = $id was not updated")
        }

    override fun deleteById(id: Int) =
        dsl.delete(USERS)
            .where(USERS.ID.eq(id))
            .execute()
            .toUnit()

    override fun findById(id: Int): User? =
        dsl.selectFrom(USERS)
            .where(USERS.ID.eq(id))
            .fetchOne()
            ?.map(userRecordMapper)

    override fun findByUsername(username: String): User? =
        dsl.selectFrom(USERS)
            .where(USERS.USERNAME.eq(username))
            .fetchOne()
            ?.map(userRecordMapper)

    override fun findByEmail(email: String): User? =
        dsl.selectFrom(USERS)
            .where(USERS.EMAIL.eq(email))
            .fetchOne()
            ?.map(userRecordMapper)

    override fun findByIds(ids: List<Int>): List<User> =
        dsl.selectFrom(USERS)
            .where(USERS.ID.`in`(ids))
            .fetch()
            .map(userRecordMapper)

    override fun findAll(): List<User> =
        dsl.selectFrom(USERS)
            .fetch()
            .map(userRecordMapper)
}