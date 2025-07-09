package cmu.group2.chargist.data.supabase.dto

import cmu.group2.chargist.data.database.entity.ChargerEntity
import kotlinx.serialization.Serializable

@Serializable
data class SupaCharger(
    val id: Long? = null,
    val stationId: Long,
    val type: String,
    val power: String,
    val price: Double,
    val status: String,
    val issue: String
)

fun SupaCharger.toEntity(): ChargerEntity {
    return ChargerEntity(
        id = id!!,
        stationId = stationId,
        type = type,
        power = power,
        price = price,
        status = status,
        issue = issue,
    )
}

