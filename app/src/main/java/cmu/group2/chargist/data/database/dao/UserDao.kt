package cmu.group2.chargist.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import cmu.group2.chargist.data.database.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: UserEntity)

    @Update
    suspend fun update(user: UserEntity)

    @Query("SELECT * FROM user WHERE id = :userId")
    fun getUserByIdFlow(userId: Long): Flow<UserEntity?>

    @Query("SELECT * FROM user WHERE id = :userId")
    suspend fun getUserById(userId: Long): UserEntity?

}