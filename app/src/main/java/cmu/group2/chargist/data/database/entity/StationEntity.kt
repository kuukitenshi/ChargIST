package cmu.group2.chargist.data.database.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import cmu.group2.chargist.data.model.GeoLocation
import cmu.group2.chargist.data.model.NearbyService
import cmu.group2.chargist.data.model.PaymentMethod
import cmu.group2.chargist.data.model.Station

@Entity(tableName = "stations")
data class StationEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val paymentMethods: String,
    val imageUrl: String?,
    val avgRating: Float,
    val nearbyServices: String
)

data class StationFull(
    @Embedded val station: StationEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "stationId",
        entity = ChargerEntity::class,
        projection = ["id", "stationId", "type", "power", "price", "status", "issue"]
    ) val chargers: List<ChargerEntity>,
    @Relation(
        parentColumn = "id",
        entityColumn = "stationId",
        entity = ReviewEntity::class,
        projection = ["stationId", "userId", "rating", "comment", "date"]
    ) val reviews: List<ReviewWithUserEntity>,
)

fun Station.toEntity(): StationEntity {
    return StationEntity(
        id = id,
        name = name,
        latitude = location.latitude,
        longitude = location.longitude,
        paymentMethods = paymentMethods.joinToString(","),
        avgRating = avgRating,
        imageUrl = imageUrl,
        nearbyServices = nearbyServices.joinToString(",")
    )
}

fun StationFull.toDomain(): Station {
    return Station(
        id = station.id,
        name = station.name,
        location = GeoLocation(station.latitude, station.longitude),
        imageUrl = station.imageUrl,
        avgRating = station.avgRating,
        chargers = chargers.map { it.toDomain() },
        reviews = reviews.map { it.toDomain() },
        paymentMethods = station.paymentMethods.split(",")
            .mapNotNull { runCatching { PaymentMethod.valueOf(it.trim()) }.getOrNull() },
        nearbyServices = station.nearbyServices.split(",")
            .mapNotNull { runCatching { NearbyService.valueOf(it.trim()) }.getOrNull() }
    )
}