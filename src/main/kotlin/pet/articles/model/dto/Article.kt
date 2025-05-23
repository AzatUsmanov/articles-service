package pet.articles.model.dto

import kotlinx.serialization.Serializable
import pet.articles.tool.serializer.LocalDateTimeSerializer
import java.time.LocalDateTime

@Serializable
data class Article(
    val id : Int?,
    @Serializable(with = LocalDateTimeSerializer::class)
    val dateOfCreation : LocalDateTime,
    val topic : String,
    val content : String
)

