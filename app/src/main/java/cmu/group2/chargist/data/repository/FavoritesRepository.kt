package cmu.group2.chargist.data.repository

import cmu.group2.chargist.data.database.AppDatabase
import cmu.group2.chargist.data.database.entity.FavoriteStationEntity
import cmu.group2.chargist.data.model.GuestUser
import cmu.group2.chargist.data.supabase.SupaTable
import cmu.group2.chargist.data.supabase.Supabase
import cmu.group2.chargist.data.supabase.dto.SupaFavorite
import cmu.group2.chargist.data.supabase.table
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

object FavoritesRepository {
    private val favDao by lazy { AppDatabase.getDatabase().favStationDao() }

    @OptIn(ExperimentalCoroutinesApi::class)
    val userFavorites = UserRepository.currentUser.filterNotNull()
        .flatMapLatest { favDao.getByUser(it.id).map { it.map { it.stationId } } }

    suspend fun addFavorite(stationId: Long, userId: Long) {
        if (userId != GuestUser.id) {
            val supaFavorite = SupaFavorite(
                stationId = stationId,
                userId = userId
            )
            Supabase.table(SupaTable.Favorites).upsert(supaFavorite) {
                onConflict = "stationId,userId"
            }
        }
        val favStation = FavoriteStationEntity(
            stationId = stationId,
            userId = userId
        )
        favDao.insert(favStation)
    }

    suspend fun removeFavorite(stationId: Long, userId: Long) {
        if (userId != GuestUser.id) {
            Supabase.table(SupaTable.Favorites).delete {
                filter {
                    eq("stationId", stationId)
                    eq("userId", userId)
                }
            }
        }
        val favStation = FavoriteStationEntity(
            stationId = stationId,
            userId = userId
        )
        favDao.delete(favStation)
    }

}