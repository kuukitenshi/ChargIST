package cmu.group2.chargist.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import cmu.group2.chargist.data.database.dao.ChargerDao
import cmu.group2.chargist.data.database.dao.FavStationsDao
import cmu.group2.chargist.data.database.dao.ReviewDao
import cmu.group2.chargist.data.database.dao.StationDao
import cmu.group2.chargist.data.database.dao.UserDao
import cmu.group2.chargist.data.database.entity.ChargerEntity
import cmu.group2.chargist.data.database.entity.FavoriteStationEntity
import cmu.group2.chargist.data.database.entity.ReviewEntity
import cmu.group2.chargist.data.database.entity.StationEntity
import cmu.group2.chargist.data.database.entity.UserEntity

@Database(
    entities = [ChargerEntity::class, StationEntity::class, UserEntity::class, ReviewEntity::class, FavoriteStationEntity::class],
    version = 1
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun stationDao(): StationDao
    abstract fun chargerDao(): ChargerDao
    abstract fun userDao(): UserDao
    abstract fun favStationDao(): FavStationsDao
    abstract fun reviewDao(): ReviewDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun init(context: Context) {
            if (INSTANCE != null) {
                throw RuntimeException("Database already initialized!")
            }
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "chargist_database"
            ).build()
            INSTANCE = instance
        }

        fun close() {
            INSTANCE?.close()
            INSTANCE = null
        }

        fun getDatabase(): AppDatabase = INSTANCE!!
    }
}
