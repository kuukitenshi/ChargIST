package cmu.group2.chargist.data.database

import androidx.room.TypeConverter
import java.util.UUID

class Converters {

    @TypeConverter
    fun fromUUID(uuid: UUID): String = uuid.toString()

    @TypeConverter
    fun toUUID(uuid: String): UUID = UUID.fromString(uuid)

}
