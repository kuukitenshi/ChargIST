package cmu.group2.chargist.data.model

import cmu.group2.chargist.calculateDistanceMeters
import cmu.group2.chargist.estimateTravelTimeSeconds
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FilterOptions {

    private val _selectedTypes = MutableStateFlow<List<ChargerType>>(emptyList())
    val selectedTypes = _selectedTypes.asStateFlow()

    private val _selectedPayments = MutableStateFlow<List<PaymentMethod>>(emptyList())
    val selectedPayments = _selectedPayments.asStateFlow()

    private val _priceRange = MutableStateFlow(0.0f..20.0f)
    val priceRange = _priceRange.asStateFlow()

    private val _selectedPowers = MutableStateFlow<List<ChargerPower>>(emptyList())
    val selectedPowers = _selectedPowers.asStateFlow()

    private val _selectedServices = MutableStateFlow<List<NearbyService>>(emptyList())
    val selectedServices = _selectedServices.asStateFlow()

    private val _onlyAvailable = MutableStateFlow(false)
    val onlyAvailable = _onlyAvailable.asStateFlow()

    private val _maxDistance = MutableStateFlow(25.0f)
    val maxDistance = _maxDistance.asStateFlow()

    private val _maxTravelTime = MutableStateFlow(30.0f)
    val maxTravelTime = _maxTravelTime.asStateFlow()

    fun updateSelectedTypes(selectedTypes: List<ChargerType>) {
        _selectedTypes.update { selectedTypes }
    }

    fun updateSelectedPayments(selectedPayments: List<PaymentMethod>) {
        _selectedPayments.update { selectedPayments }
    }

    fun updatePriceRange(priceRange: ClosedFloatingPointRange<Float>) {
        _priceRange.update { priceRange }
    }

    fun updateSelectedPowers(selectedPowers: List<ChargerPower>) {
        _selectedPowers.update { selectedPowers }
    }

    fun updateSelectedServices(selectedServices: List<NearbyService>) {
        _selectedServices.update { selectedServices }
    }

    fun updateOnlyAvailable(onlyAvailable: Boolean) {
        _onlyAvailable.update { onlyAvailable }
    }

    fun updateMaxDistance(maxDistance: Float) {
        _maxDistance.update { maxDistance }
    }

    fun updateMaxTravelTime(maxTravelTime: Float) {
        _maxTravelTime.update { maxTravelTime }
    }

    fun toFilterFunction(userLocation: GeoLocation): (Station) -> Boolean {
        return { station ->
            val matchesType =
                selectedTypes.value.isEmpty() || station.chargers.any { it.type in selectedTypes.value }
            val matchesPayment =
                selectedPayments.value.isEmpty() || station.paymentMethods.any { it in selectedPayments.value }
            val matchesPower =
                selectedTypes.value.isEmpty() || station.chargers.any { it.power in selectedPowers.value }
            val matchesPrice =
                (station.chargers.minOfOrNull { it.price } ?: Double.MAX_VALUE) in priceRange.value
            val matchesServices =
                selectedServices.value.isEmpty() || selectedServices.value.all { it in station.nearbyServices }
            val matchesAvailability =
                !onlyAvailable.value || station.chargers.any { it.status == ChargerStatus.FREE }

            val distanceMeters = calculateDistanceMeters(userLocation, station.location)
            val matchesDistance = distanceMeters <= maxDistance.value * 1000f

            val travelTimeMinutes = estimateTravelTimeSeconds(distanceMeters) / 60f
            val matchesTravelTime = travelTimeMinutes <= maxTravelTime.value

            matchesType && matchesPayment && matchesPower &&
                    matchesPrice && matchesServices &&
                    matchesAvailability && matchesDistance && matchesTravelTime
        }
    }

    fun toState(): FilterOptionsState {
        return FilterOptionsState(
            onlyAvailable = onlyAvailable.value,
            chargerTypes = selectedTypes.value,
            chargerPower = selectedPowers.value,
            priceRange = priceRange.value,
            paymentMethods = selectedPayments.value,
            nearbyServices = selectedServices.value,
            maxDistance = maxDistance.value
        )
    }

}

data class FilterOptionsState(
    val onlyAvailable: Boolean,
    val chargerTypes: List<ChargerType>,
    val chargerPower: List<ChargerPower>,
    val priceRange: ClosedFloatingPointRange<Float>,
    val paymentMethods: List<PaymentMethod>,
    val nearbyServices: List<NearbyService>,
    val maxDistance: Float,
)
