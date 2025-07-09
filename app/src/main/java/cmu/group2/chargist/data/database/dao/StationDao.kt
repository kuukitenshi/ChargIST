package cmu.group2.chargist.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import cmu.group2.chargist.data.database.entity.StationEntity
import cmu.group2.chargist.data.database.entity.StationFull
import kotlinx.coroutines.flow.Flow

@Dao
interface StationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(station: StationEntity)

    @Delete
    suspend fun delete(station: StationEntity)

    @Update
    suspend fun update(station: StationEntity)

    @Query("SELECT * FROM stations")
    fun getAllStation(): List<StationEntity>

    @Transaction
    @Query("SELECT * FROM stations")
    fun getAllStationFullFlow(): Flow<List<StationFull>>

    @Transaction
    @Query("SELECT * FROM stations WHERE id = :stationId")
    fun getStationFullByIdFlow(stationId: Long): Flow<StationFull?>

    @Query("SELECT * FROM stations WHERE id = :stationId")
    suspend fun getStationFullById(stationId: Long): StationFull?

}
