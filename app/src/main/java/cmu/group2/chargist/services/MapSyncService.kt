package cmu.group2.chargist.services

import android.util.Log
import cmu.group2.chargist.calculateDistanceMeters
import cmu.group2.chargist.data.database.AppDatabase
import cmu.group2.chargist.data.model.FilterOptionsState
import cmu.group2.chargist.data.model.GeoLocation
import cmu.group2.chargist.data.supabase.SupaTable
import cmu.group2.chargist.data.supabase.Supabase
import cmu.group2.chargist.data.supabase.dto.SupaFilter
import cmu.group2.chargist.data.supabase.dto.SupaFilterResponse
import cmu.group2.chargist.data.supabase.dto.SupaStation
import cmu.group2.chargist.data.supabase.dto.toEntity
import cmu.group2.chargist.data.supabase.table
import cmu.group2.chargist.isOnMeteredConnection
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import kotlin.math.cos

@OptIn(FlowPreview::class)
class MapSyncService : AbstractCoroutineService() {
    private val SEARCH_RADIUS_METERS = 30000.0 // 30kms
    private val KMS_PER_DEGREE = 111000.0
    private val MAP_CENTER_STABILITY_THRESHOLD = 3
    private val POLLING_TIME_MILLIS = 2000L // 3s

    companion object {
        private val stationDao by lazy { AppDatabase.getDatabase().stationDao() }
        private val chargerDao by lazy { AppDatabase.getDatabase().chargerDao() }
        private val coScope = CoroutineScope(Dispatchers.IO)

        private val currentLocation = LocationService.currentLocation.stateIn(
            scope = coScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

        private val _mapDragLocation = MutableStateFlow<GeoLocation?>(null)
        val mapDragLocation = _mapDragLocation.asStateFlow()

        private val _filterState = MutableStateFlow<FilterOptionsState?>(null)

        fun updateMapDragLocation(location: GeoLocation?) {
            _mapDragLocation.update { location }
        }

        fun fetchFromSupaFilter(filter: FilterOptionsState) {
            currentLocation.value?.let {
                coScope.launch {
                    val allStations = stationDao.getAllStation()
                    try {
                        val supaFilter = SupaFilter(
                            only_available = filter.onlyAvailable,
                            charger_types = filter.chargerTypes.map { it.name },
                            charger_speeds = filter.chargerPower.map { it.name },
                            min_price = filter.priceRange.start,
                            max_price = filter.priceRange.endInclusive,
                            payment_methods = filter.paymentMethods.map { it.name },
                            nearby_services = filter.nearbyServices.map { it.name },
                            already_have = allStations.map { it.id },
                            max_distance = 100.0f,
                            user_latitude = it.latitude,
                            user_longitude = it.longitude,
                        )
                        Log.d("MapSyncService", "already have: ${allStations.map { it.id }}")
                        val json = Json.encodeToJsonElement(supaFilter)
                        val response = Supabase.postgrest.rpc("stations_filter", json.jsonObject)
                        val filtered = response.decodeList<SupaFilterResponse>()
                        filtered.forEach {
                            val entity = it.station.toEntity()
                            chargerDao.deleteByStation(entity.id)
                            stationDao.insert(entity)
                            it.chargers.forEach { chargerDao.insert(it.toEntity()) }
                        }
                        Log.d(
                            "MapSyncService",
                            "fetched from filter: ${filtered.map { it.station.id }}"
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }


    init {
        // ----------------- Stations by close proximity -----------------
        serviceScope.launch {
            currentLocation
                .filterNotNull()
                .debounce(5000L)
                .collect { location ->
                    Log.d("MAPSYNC", "Current location changed (sampled): $location")
                    fetchNearbyStations(GeoLocation(location.latitude, location.longitude))
                }
        }

        // ----------------- station vision -----------------
        serviceScope.launch {
            var lastCenter: GeoLocation? = null
            var stableCount = 0

            while (true) {
                val center = mapDragLocation.value
                if (center != null) {
                    if (lastCenter != null && calculateDistanceMeters(center, lastCenter) < 50) {
                        stableCount++
                    } else {
                        stableCount = 1
                        lastCenter = center
                    }

                    if (stableCount >= MAP_CENTER_STABILITY_THRESHOLD) {
                        Log.d("MapSyncService", "fetching by map vision")
                        fetchNearbyStations(center)
                        stableCount = 0
                    }
                }
                delay(POLLING_TIME_MILLIS)
            }
        }

        serviceScope.launch {
            _filterState.filterNotNull().collect {
                Log.d("MapSyncService", "Filter updated")
                fetchFromSupaFilter(it)
            }
        }
    }

    private suspend fun fetchNearbyStations(center: GeoLocation) {
        try {
            val latDelta = SEARCH_RADIUS_METERS / KMS_PER_DEGREE
            val lngDelta =
                SEARCH_RADIUS_METERS / (KMS_PER_DEGREE * cos(Math.toRadians(center.latitude)))
            val metered = application.applicationContext.isOnMeteredConnection()
            val columns = if (metered) {
                Columns.list("id", "name", "latitude", "longitude", "avgRating")
            } else {
                Columns.ALL
            }

            val supaStations = Supabase.table(SupaTable.Stations).select(
                columns = columns
            ) {
                filter {
                    and {
                        gte("latitude", center.latitude - latDelta)
                        lte("latitude", center.latitude + latDelta)
                        gte("longitude", center.longitude - lngDelta)
                        lte("longitude", center.longitude + lngDelta)
                    }
                }
            }.decodeList<SupaStation>()

            supaStations.map { it.toEntity() }.forEach { entity ->
                val existing = stationDao.getStationFullById(entity.id)
                if (existing == null) {
                    stationDao.insert(entity)
                } else {
                    val updated = existing.station.copy(
                        name = entity.name,
                        latitude = entity.latitude,
                        longitude = entity.longitude,
                        avgRating = entity.avgRating
                    )
                    stationDao.update(updated)
                }
            }
        } catch (_: Exception) {
        }
    }

}
