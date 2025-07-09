package cmu.group2.chargist.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import cmu.group2.chargist.data.database.entity.ChargerEntity

@Dao
interface ChargerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(charger: ChargerEntity)

    @Update
    suspend fun update(charger: ChargerEntity)

    @Delete
    suspend fun delete(charger: ChargerEntity)

    @Query("DELETE FROM chargers WHERE stationId = :stationId")
    suspend fun deleteByStation(stationId: Long)
}