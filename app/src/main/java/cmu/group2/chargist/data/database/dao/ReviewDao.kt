package cmu.group2.chargist.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cmu.group2.chargist.data.database.entity.ReviewEntity

@Dao
interface ReviewDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(review: ReviewEntity)

    @Delete
    suspend fun delete(review: ReviewEntity)

    @Query("DELETE FROM reviews WHERE stationId = :stationId")
    suspend fun deleteByStation(stationId: Long)

    @Query("SELECT COUNT(*) FROM reviews WHERE stationId = :stationId AND comment != \"\"")
    suspend fun countCommentReviewsInStation(stationId: Long): Long

}