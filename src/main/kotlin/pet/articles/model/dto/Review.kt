package pet.articles.model.dto

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import pet.articles.model.enums.ReviewType
import pet.articles.tool.serializer.LocalDateTimeSerializer

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Serializable
data class Review(
    val id : Int?,
    val type : ReviewType,
    @Serializable(with = LocalDateTimeSerializer::class)
    val dateOfCreation : LocalDateTime,
    val content : String,
    val authorId : Int?,
    val articleId : Int
)
