package cmu.group2.chargist.services

import android.util.Log
import cmu.group2.chargist.data.database.AppDatabase
import cmu.group2.chargist.data.model.User
import cmu.group2.chargist.data.repository.UserRepository
import cmu.group2.chargist.data.supabase.SupaTable
import cmu.group2.chargist.data.supabase.Supabase
import cmu.group2.chargist.data.supabase.dto.SupaFavorite
import cmu.group2.chargist.data.supabase.dto.toEntity
import cmu.group2.chargist.data.supabase.table
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.postgrest.query.filter.FilterOperation
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.realtime.selectAsFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FavoritesSyncService : AbstractCoroutineService() {
    private val userRepository = UserRepository
    private val favDao by lazy { AppDatabase.getDatabase().favStationDao() }

    private var favoritesSyncJob: Job? = null

    private val currentUser =
        userRepository.currentUser.filter { it == null || !it.isGuest }.stateIn(
            scope = serviceScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    @OptIn(ExperimentalCoroutinesApi::class, SupabaseExperimental::class)
    private val favorites = currentUser.filterNotNull().flatMapLatest { user ->
        Supabase.table(SupaTable.Favorites).selectAsFlow(
            primaryKeys = listOf(SupaFavorite::stationId, SupaFavorite::userId),
            filter = FilterOperation(
                "userId",
                FilterOperator.EQ,
                user.id
            )
        )
    }.stateIn(
        scope = serviceScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        serviceScope.launch {
            currentUser.collect { user ->
                favoritesSyncJob?.cancel()
                user?.let {
                    favoritesSyncJob = startFavoritesSync(it)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        favoritesSyncJob?.cancel()
    }

    private fun startFavoritesSync(user: User): Job {
        Log.d(this::class.simpleName, "Started sync service for user ${user.username}")
        return serviceScope.launch {
            while (true) {
                try {
                    favDao.deleteByUser(user.id)
                    favorites.collect { supaFavs ->
                        supaFavs.forEach {
                            favDao.insert(it.toEntity())
                        }
                    }
                } catch (_: Exception) {
                    delay(2000L)
                }
            }
        }
    }
}