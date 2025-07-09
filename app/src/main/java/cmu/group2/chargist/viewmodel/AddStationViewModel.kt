package cmu.group2.chargist.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.os.Looper
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cmu.group2.chargist.data.api.GeoResponse
import cmu.group2.chargist.data.api.NominatimApi
import cmu.group2.chargist.data.model.GeoLocation
import cmu.group2.chargist.data.model.StationFormData
import cmu.group2.chargist.data.repository.StationRepository
import cmu.group2.chargist.services.SearchSyncService
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.await

@OptIn(FlowPreview::class)
class AddStationViewModel(application: Application) : AndroidViewModel(application) {
    private val stationRepository = StationRepository

    private val _lastDragLocation = MutableStateFlow<GeoLocation?>(null)
    val lastDragLocation: StateFlow<GeoLocation?> = _lastDragLocation

    private val _lastZoom = MutableStateFlow<Double?>(null)
    val lastZoom: StateFlow<Double?> = _lastZoom

    private val _userCurrentLocation = MutableStateFlow<GeoLocation?>(null)
    val userCurrentLocation: StateFlow<GeoLocation?> = _userCurrentLocation

    private val locationClient =
        LocationServices.getFusedLocationProviderClient(getApplication<Application>().applicationContext)

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(location: LocationResult) {
            val loc = location.lastLocation
            if (loc != null) {
                _userCurrentLocation.update { GeoLocation(loc.latitude, loc.longitude) }
            }
        }
    }

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

    @SuppressLint("MissingPermission")
    fun locationGranted() {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).build()
        locationClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
    }

    fun onDrag(location: GeoLocation) {
        _lastDragLocation.update { location }
    }

    fun onZoom(zoom: Double) {
        _lastZoom.update { zoom }
    }

    fun submitStation(data: StationFormData, onSuccess: () -> Unit, onFail: () -> Unit) {
        viewModelScope.launch {
            try {
                stationRepository.addStation(data)
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
                onFail()
            }
        }
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