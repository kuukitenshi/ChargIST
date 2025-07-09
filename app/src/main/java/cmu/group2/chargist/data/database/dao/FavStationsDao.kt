package cmu.group2.chargist.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cmu.group2.chargist.data.database.entity.FavoriteStationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavStationsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(favorite: FavoriteStationEntity)

    @Delete
    suspend fun delete(favorite: FavoriteStationEntity)

    @Query("DELETE FROM favorite_stations WHERE userId = :userId")
    suspend fun deleteByUser(userId: Long)

    @Query("SELECT * FROM favorite_stations WHERE userId = :userId")
    fun getByUser(userId: Long): Flow<List<FavoriteStationEntity>>

}