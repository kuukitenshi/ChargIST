package cmu.group2.chargist.data.supabase.dto

import cmu.group2.chargist.data.database.entity.ReviewEntity
import kotlinx.serialization.Serializable

@Serializable
data class SupaReview(
    val stationId: Long,
    val userId: Long,
    val rating: Int,
    val comment: String,
    val date: Long
)

fun SupaReview.toEntity(): ReviewEntity {
    return ReviewEntity(
        stationId = stationId,
        userId = userId,
        rating = rating,
        comment = comment,
        date = date
    )
}
