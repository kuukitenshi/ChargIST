package cmu.group2.chargist.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cmu.group2.chargist.data.api.GeoResponse
import cmu.group2.chargist.data.api.NominatimApi
import cmu.group2.chargist.data.model.GeoLocation
import cmu.group2.chargist.data.model.Station
import cmu.group2.chargist.data.model.StationFormData
import cmu.group2.chargist.data.repository.StationRepository
import cmu.group2.chargist.services.SearchSyncService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.await

@OptIn(FlowPreview::class)
class EditStationDetailsViewModel : ViewModel() {
    private val stationRepository = StationRepository

    private val _lastDragLocation = MutableStateFlow<GeoLocation?>(null)
    val lastDragLocation: StateFlow<GeoLocation?> = _lastDragLocation

    private val _lastZoom = MutableStateFlow<Double?>(null)
    val lastZoom: StateFlow<Double?> = _lastZoom

    private val _stationId = MutableStateFlow<Long?>(null)

    val station = _stationId.filterNotNull().map {
        stationRepository.getStationByIdInstance(it)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    // --------------- search bar picker ----------------
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
    }

    fun fetchStation(stationId: Long) {
        _stationId.update { stationId }
    }

    fun updateStation(
        station: Station,
        formData: StationFormData,
        onComplete: () -> Unit,
        onFail: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                formData.location?.let {
                    stationRepository.updateStation(station, formData)
                }
                onComplete()
            } catch (_: Exception) {
                onFail()
            }
        }
    }

    fun onDrag(location: GeoLocation) {
        _lastDragLocation.update { location }
    }

    fun onZoom(zoom: Double) {
        _lastZoom.update { zoom }
    }

    // --------------- search bar picker ----------------
    fun onSearchChange(newQuery: String) {
        _searchQuery.update { newQuery }
    }

    fun onClearSearch() {
        _searchQuery.update { "" }
        _searchResults.update { emptyList() }
    }
}
