package pet.articles.tool.jooq.unmapper

import org.jooq.RecordUnmapper
import org.koin.core.annotation.Single


import pet.articles.generated.jooq.tables.records.UsersRecord
import pet.articles.model.dto.User

class UserRecordUnmapper : RecordUnmapper<User, UsersRecord> {

    override fun unmap(source: User?): UsersRecord =
        UsersRecord(
            id = source!!.id,
            username = source.username,
            email = source.email,
            password = source.password!!,
            role = source.role.ordinal.toShort()
        )
}