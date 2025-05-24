package pet.articles.repository

import org.jooq.DSLContext
import org.jooq.RecordMapper
import org.jooq.RecordUnmapper
import org.jooq.Record
import org.jooq.exception.DataAccessException

import pet.articles.generated.jooq.tables.records.UsersRecord
import pet.articles.generated.jooq.tables.references.USERS
import pet.articles.model.dto.User

class UserRepositoryImpl(
    private val dsl: DSLContext,
    private val userRecordMapper: RecordMapper<Record, User>,
    userRecordUnmapper: RecordUnmapper<User, UsersRecord>
) : UserRepository, CrudJooqRepository<User, UsersRecord>(
    dsl = dsl,
    table = USERS,
    recordMapper = userRecordMapper,
    recordUnmapper = userRecordUnmapper,
    idField = USERS.ID
) {
    override fun updateById(item: User, id: Int): User =
        dsl.transactionResult { config ->
            config.dsl()
                .update(USERS)
                .set(USERS.USERNAME, item.username)
                .set(USERS.EMAIL, item.email)
                .set(USERS.PASSWORD, item.password)
                .set(USERS.ROLE, item.role.ordinal.toShort())
                .where(USERS.ID.eq(id))
                .returning()
                .fetchOne()
                ?.map(userRecordMapper)
                ?: throw DataAccessException("User with id = $id was not updated")
        }

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
}