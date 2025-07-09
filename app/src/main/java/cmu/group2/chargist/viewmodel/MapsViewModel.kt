package cmu.group2.chargist.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cmu.group2.chargist.data.api.GeoResponse
import cmu.group2.chargist.data.api.NominatimApi
import cmu.group2.chargist.data.model.FilterOptions
import cmu.group2.chargist.data.model.GeoLocation
import cmu.group2.chargist.data.model.SOptions
import cmu.group2.chargist.data.model.Station
import cmu.group2.chargist.data.model.toSortComparator
import cmu.group2.chargist.data.repository.FavoritesRepository
import cmu.group2.chargist.data.repository.StationRepository
import cmu.group2.chargist.services.LocationService
import cmu.group2.chargist.services.MapSyncService
import cmu.group2.chargist.services.SearchSyncService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.await

@OptIn(FlowPreview::class)
class MapsViewModel(application: Application) : AndroidViewModel(application) {
    private val stationRepository = StationRepository
    private val favoritesRepository = FavoritesRepository

    val currentLocation = LocationService.currentLocation.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val lastDragLocation = PreservedMapState.lastDragLocation.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val allStations = stationRepository.stations.debounce(3000L)
        .combine(favoritesRepository.userFavorites) { s, f ->
            s.map { Pair(it, f.contains(it.id)) }
        }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val lastZoom = PreservedMapState.lastZoom.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    private val _shouldCenterMap = MutableStateFlow(false)
    val shouldCenterMap = _shouldCenterMap.asStateFlow()

    private val _filterOptions = MutableStateFlow(FilterOptions())
    val filterOptions = _filterOptions.asStateFlow()

    private val _filterFunc = MutableStateFlow<(Station) -> Boolean> { true }
    val filterFunc = _filterFunc.asStateFlow()

    private val _selectedSortOption = MutableStateFlow(SOptions.NEAREST)
    val selectedSortOption = _selectedSortOption.asStateFlow()

    val sortComparator =
        selectedSortOption.map { it.toSortComparator(currentLocation.value) }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SOptions.NEAREST.toSortComparator(null)
        )

    private val _searchQuery = MutableStateFlow("")
    private val _searchResults = MutableStateFlow<List<GeoResponse>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            _searchQuery
                .debounce(1500)
                .collect { query ->
                    if (query.isEmpty())
                        _searchResults.update { emptyList() }
                    else {
                        try {
                            val results = NominatimApi.search(query).await()
                            _searchResults.update { results }
                            SearchSyncService.updateSearchQuery(query)
                        } catch (e: Exception) {
                            _searchResults.update { emptyList() }
                            e.printStackTrace()
                        }
                    }
                }
        }
        viewModelScope.launch {
            currentLocation.collect {
                if (it != null && !PreservedMapState.alreadyCentered) {
                    _shouldCenterMap.update { true }
                }
            }
        }
    }

    fun onDrag(location: GeoLocation) {
        PreservedMapState.setLastDragLocation(location)
        MapSyncService.updateMapDragLocation(location)
    }

    fun onZoom(zoom: Double) {
        PreservedMapState.setLastZoom(zoom)
    }

    fun onCenter() {
        PreservedMapState.alreadyCentered = true
        _shouldCenterMap.update { false }
    }

    fun clearFilterOptions() {
        _filterOptions.update { FilterOptions() }
        _filterFunc.update { { true } }
    }

    fun applySortOptions(option: SOptions) {
        _selectedSortOption.update { option }
    }

    fun applyFilter() {
        val userLoc = currentLocation.value?.let { GeoLocation(it.latitude, it.longitude) }
        val filterFunc = filterOptions.value.toFilterFunction(userLoc ?: GeoLocation())
        MapSyncService.fetchFromSupaFilter(filterOptions.value.toState())
        _filterFunc.update { filterFunc }
    }

    fun onLoadMore() {
        MapSyncService.fetchFromSupaFilter(filterOptions.value.toState())
    }

    fun onSearchChange(newQuery: String) {
        _searchQuery.update { newQuery }
    }

    fun onClearSearch() {
        _searchQuery.update { "" }
        _searchResults.update { emptyList() }
    }
}

object PreservedMapState {

    var alreadyCentered = false

    private val _lastDragLocation = MutableStateFlow<GeoLocation?>(null)
    val lastDragLocation = _lastDragLocation.asStateFlow()

    private val _lastZoom = MutableStateFlow<Double?>(null)
    val lastZoom = _lastZoom.asStateFlow()

    fun setLastDragLocation(location: GeoLocation) {
        _lastDragLocation.update { location }
    }

    fun setLastZoom(zoom: Double) {
        _lastZoom.update { zoom }
    }

}