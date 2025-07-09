package cmu.group2.chargist.data.supabase.dto

import cmu.group2.chargist.data.database.entity.FavoriteStationEntity
import kotlinx.serialization.Serializable

@Serializable
data class SupaFavorite(
    val stationId: Long,
    val userId: Long
)

fun SupaFavorite.toEntity(): FavoriteStationEntity {
    return FavoriteStationEntity(
        stationId = stationId,
        userId = userId
    )
}
