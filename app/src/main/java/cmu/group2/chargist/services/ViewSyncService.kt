package cmu.group2.chargist.services

import android.util.Log
import cmu.group2.chargist.data.database.AppDatabase
import cmu.group2.chargist.data.supabase.SupaTable
import cmu.group2.chargist.data.supabase.Supabase
import cmu.group2.chargist.data.supabase.dto.SupaCharger
import cmu.group2.chargist.data.supabase.dto.SupaReview
import cmu.group2.chargist.data.supabase.dto.SupaStation
import cmu.group2.chargist.data.supabase.dto.SupaUser
import cmu.group2.chargist.data.supabase.dto.toEntity
import cmu.group2.chargist.data.supabase.table
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.query.filter.FilterOperation
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.realtime.selectAsFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(SupabaseExperimental::class)
class ViewSyncService : AbstractCoroutineService() {
    private val userDao by lazy { AppDatabase.getDatabase().userDao() }
    private val stationDao by lazy { AppDatabase.getDatabase().stationDao() }
    private val chargerDao by lazy { AppDatabase.getDatabase().chargerDao() }
    private val reviewDao by lazy { AppDatabase.getDatabase().reviewDao() }
    private val LIMIT_PAGE: Long = 3

    companion object {
        private val _viewingStationId = MutableStateFlow<Long?>(null)
        val viewingStationId = _viewingStationId.asStateFlow()

        private val _pagedReviews = MutableStateFlow<List<SupaReview>>(emptyList())
        val pagedReviews = _pagedReviews.asStateFlow()

        private val _offsetPage = MutableStateFlow<Long>(0)
        val offsetPage = _offsetPage.asStateFlow()

        private val _loadPageCommand = MutableStateFlow<Pair<Long, Long>?>(null)
        val loadPageCommand = _loadPageCommand.asStateFlow()

        fun syncStation(stationId: Long) {
            _viewingStationId.update { stationId }
        }

        fun stopStationSync() {
            _viewingStationId.update { null }
        }

        fun loadPageReviews(stationId: Long, offset: Long) {
            _loadPageCommand.value = Pair(stationId, offset)
        }
    }

    private var stationSyncScope: CoroutineScope? = null
    private var chargerSyncScope: CoroutineScope? = null
    private var limitedReviewSyncScope: CoroutineScope? = null

    init {
        serviceScope.launch {
            try {
                viewingStationId.collect {
                    stationSyncScope?.cancel()
                    chargerSyncScope?.cancel()
                    limitedReviewSyncScope?.cancel()
                    it?.let { stationId ->
                        stationSyncScope = startStationSync(stationId)
                        chargerSyncScope = startChargersSync(stationId)
                        limitedReviewSyncScope = startLimitedReviewsSync(
                            stationId = stationId,
                            offset = offsetPage.value
                        )
                    }
                }
            } catch (_: Exception) {
            }
        }

        serviceScope.launch {
            loadPageCommand.filterNotNull().collect { (stationId, offset) ->
                loadPageReviewsInter(stationId, offset)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stationSyncScope?.cancel()
        chargerSyncScope?.cancel()
        limitedReviewSyncScope?.cancel()
    }

    @OptIn(SupabaseExperimental::class)
    fun startStationSync(stationId: Long): CoroutineScope {
        val coroutine = CoroutineScope(Dispatchers.IO)
        coroutine.launch {
            try {
                val supaStation = Supabase.table(SupaTable.Stations).selectAsFlow(
                    primaryKey = SupaStation::id,
                    filter = FilterOperation(
                        "id",
                        FilterOperator.EQ,
                        stationId
                    )
                ).map { it.firstOrNull() }.stateIn(
                    scope = this,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = null
                )
                supaStation.filterNotNull().collect {
                    stationDao.insert(it.toEntity())
                }
            } catch (_: Exception) {
            }
        }
        return coroutine
    }

    @OptIn(SupabaseExperimental::class)
    fun startChargersSync(stationId: Long): CoroutineScope {
        val coroutine = CoroutineScope(Dispatchers.IO)
        coroutine.launch {
            try {
                val supaChargers = Supabase.table(SupaTable.Chargers).selectAsFlow(
                    primaryKey = SupaCharger::id,
                    filter = FilterOperation(
                        "stationId",
                        FilterOperator.EQ,
                        stationId
                    )
                ).stateIn(
                    scope = this,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = emptyList()
                )
                supaChargers.collect {
                    chargerDao.deleteByStation(stationId)
                    it.forEach { charger ->
                        chargerDao.insert(charger.toEntity())
                    }
                }
            } catch (_: Exception) {
            }
        }
        return coroutine
    }

    private suspend fun fetchAndInsertReviews(
        stationId: Long,
        offset: Long,
        limit: Long
    ): List<SupaReview> {
        val reviewsPage = Supabase.table(SupaTable.Reviews).select {
            filter { eq("stationId", stationId) }
            order("date", Order.ASCENDING)
            range(offset, offset + limit - 1)
        }.decodeList<SupaReview>()

        reviewsPage.forEach {
            try {
                val supaUser = Supabase.table(SupaTable.Users).select {
                    filter { eq("id", it.userId) }
                }.decodeSingle<SupaUser>()
                userDao.insert(supaUser.toEntity())
                reviewDao.insert(it.toEntity())
            } catch (e: Exception) {
                Log.e("ReviewSync", "Error inserting user/review: ${e.message}")
            }
        }
        return reviewsPage
    }

    private fun startLimitedReviewsSync(
        stationId: Long,
        offset: Long,
        pageSize: Long = 3
    ): CoroutineScope {
        val coroutine = CoroutineScope(Dispatchers.IO + SupervisorJob())
        coroutine.launch {
            try {
                var localCount = reviewDao.countCommentReviewsInStation(stationId)
                Log.d("ReviewSync", "Local count for station $stationId: $localCount")

                var currentOffset = offset
                while (localCount < 4) {
                    val reviewsPage = fetchAndInsertReviews(stationId, currentOffset, pageSize)
                    _offsetPage.update { currentOffset + pageSize }

                    localCount = reviewDao.countCommentReviewsInStation(stationId)
                    if (reviewsPage.isEmpty()) {
                        Log.d("ReviewSync", "No more reviews to fetch, breaking loop.")
                        break
                    }
                    currentOffset += pageSize
                    delay(5000)
                }
            } catch (_: Exception) {
            }
        }
        return coroutine
    }

    private suspend fun loadPageReviewsInter(
        stationId: Long,
        offset: Long,
        limit: Long = LIMIT_PAGE
    ) {
        Log.d("ReviewSync", "Fetching reviews from offset $offset")
        val reviewsPage = fetchAndInsertReviews(stationId, offset, limit)
        _pagedReviews.update { reviewsPage }
        Log.d("ReviewSync", "Loaded ${reviewsPage.size} reviews")
    }

}