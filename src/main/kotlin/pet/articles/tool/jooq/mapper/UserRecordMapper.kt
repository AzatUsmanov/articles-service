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
            password = record[USERS.PASSWORD],
            role = record[USERS.ROLE]!!
                .toInt()
                .let { roleIndex -> UserRole.entries[roleIndex] }
        )
}
