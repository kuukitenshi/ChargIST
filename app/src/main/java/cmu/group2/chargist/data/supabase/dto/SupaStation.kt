package cmu.group2.chargist.data.supabase.dto

import cmu.group2.chargist.data.database.entity.StationEntity
import kotlinx.serialization.Serializable

@Serializable
data class SupaStation(
    val id: Long? = null,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val paymentMethods: String = "",
    val nearbyServices: String? = null,
    val imageUrl: String? = null,
    val avgRating: Float = 0f
)

fun SupaStation.toEntity(): StationEntity {
    return StationEntity(
        id = id!!,
        name = name,
        latitude = latitude,
        longitude = longitude,
        imageUrl = imageUrl,
        avgRating = avgRating,
        paymentMethods = paymentMethods,
        nearbyServices = nearbyServices ?: ""
    )
}

