package cmu.group2.chargist.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import cmu.group2.chargist.data.model.Charger
import cmu.group2.chargist.data.model.ChargerIssue
import cmu.group2.chargist.data.model.ChargerPower
import cmu.group2.chargist.data.model.ChargerStatus
import cmu.group2.chargist.data.model.ChargerType

@Entity(tableName = "chargers")
data class ChargerEntity(
    @PrimaryKey val id: Long,
    val stationId: Long,
    val type: String,
    val power: String,
    val price: Double,
    val status: String,
    val issue: String
)

fun ChargerEntity.toDomain(): Charger {
    return Charger(
        id = id,
        type = ChargerType.valueOf(type),
        power = ChargerPower.valueOf(power),
        price = price,
        issue = ChargerIssue.valueOf(issue),
        status = ChargerStatus.valueOf(status)
    )
}

fun Charger.toEntity(stationId: Long): ChargerEntity {
    return ChargerEntity(
        id = id,
        stationId = stationId,
        type = type.name,
        power = power.name,
        price = price,
        status = status.name,
        issue = issue.name
    )
}
