package cmu.group2.chargist.data.database.entity

import androidx.room.Entity

@Entity(tableName = "favorite_stations", primaryKeys = ["stationId", "userId"])
data class FavoriteStationEntity(
    val stationId: Long,
    val userId: Long
)