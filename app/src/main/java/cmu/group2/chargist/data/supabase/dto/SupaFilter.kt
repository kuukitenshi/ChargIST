package cmu.group2.chargist.data.supabase.dto

import kotlinx.serialization.Serializable

@Serializable
data class SupaFilter(
    val only_available: Boolean,
    val charger_types: List<String>,
    val charger_speeds: List<String>,
    val min_price: Float,
    val max_price: Float,
    val payment_methods: List<String>,
    val nearby_services: List<String>,
    val already_have: List<Long>,
    val max_distance: Float,
    val user_latitude: Double,
    val user_longitude: Double
)

@Serializable
data class SupaFilterResponse(
    val station: SupaStation,
    val chargers: List<SupaCharger>
)