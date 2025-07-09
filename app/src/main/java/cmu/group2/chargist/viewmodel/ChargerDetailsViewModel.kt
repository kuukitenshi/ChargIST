package cmu.group2.chargist.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cmu.group2.chargist.bundleChargers
import cmu.group2.chargist.data.model.Charger
import cmu.group2.chargist.data.model.ChargerIssue
import cmu.group2.chargist.data.model.ChargerStatus
import cmu.group2.chargist.data.repository.ChargerRepository
import cmu.group2.chargist.data.repository.StationRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChargerDetailsViewModel : ViewModel() {
    private val stationRepository = StationRepository
    private val chargerRepository = ChargerRepository

    private val _stationId = MutableStateFlow<Long?>(null)
    private val _filter = MutableStateFlow<(Charger) -> Boolean> { true }

    @OptIn(ExperimentalCoroutinesApi::class)
    val station = _stationId.filterNotNull().flatMapLatest {
        stationRepository.getStationById(it)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val bundles = station.filterNotNull().combine(_filter) { s, f -> Pair(s, f) }.map { (s, f) ->
        bundleChargers(s.chargers, f)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun fetchStation(stationId: Long) {
        _stationId.update { stationId }
    }

    fun updateFilter(filter: (Charger) -> Boolean) {
        _filter.update { filter }
    }

    fun updateChargerStatus(charger: Charger, status: ChargerStatus, onFail: () -> Unit) {
        viewModelScope.launch {
            try {
                chargerRepository.updateCharger(charger.copy(status = status))
            } catch (_: Exception) {
                onFail()
            }
        }
    }

    fun reportChargerIssue(charger: Charger, issue: ChargerIssue, onFail: () -> Unit) {
        viewModelScope.launch {
            try {
                chargerRepository.updateCharger(
                    charger.copy(
                        status = ChargerStatus.BROKEN, issue = issue
                    )
                )
            } catch (_: Exception) {
                onFail()
            }
        }
    }

    fun repairCharger(charger: Charger, onFail: () -> Unit) {
        viewModelScope.launch {
            try {
                chargerRepository.updateCharger(
                    charger.copy(
                        status = ChargerStatus.FREE, issue = ChargerIssue.FINE
                    )
                )
            } catch (_: Exception) {
                onFail()
            }
        }
    }
}