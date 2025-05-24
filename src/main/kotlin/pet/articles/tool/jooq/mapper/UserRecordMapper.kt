package pet.articles.tool.jooq.mapper

import org.jooq.RecordMapper
import org.jooq.Record
import org.koin.core.annotation.Single

import pet.articles.generated.jooq.tables.references.USERS
import pet.articles.model.dto.User
import pet.articles.model.enums.UserRole

class UserRecordMapper : RecordMapper<Record, User> {

    override fun map(record: Record): User =
        User(
            id = record[USERS.ID],
            username = record[USERS.USERNAME]!!,
            email = record[USERS.EMAIL]!!,
            role = UserRole.entries[record[USERS.ROLE]!!.toInt()],
            password = record[USERS.PASSWORD]
        )
}
