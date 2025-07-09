package cmu.group2.chargist.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cmu.group2.chargist.bundleChargers
import cmu.group2.chargist.data.api.TranslateRequest
import cmu.group2.chargist.data.api.TranslationApi
import cmu.group2.chargist.data.model.Review
import cmu.group2.chargist.data.model.Station
import cmu.group2.chargist.data.repository.FavoritesRepository
import cmu.group2.chargist.data.repository.ReviewsRepository
import cmu.group2.chargist.data.repository.StationRepository
import cmu.group2.chargist.data.repository.UserRepository
import cmu.group2.chargist.services.ViewSyncService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.awaitResponse
import java.util.Date
import java.util.Locale

class StationDetailsViewModel : ViewModel() {
    private val stationRepository = StationRepository
    private val favoritesRepository = FavoritesRepository
    private val reviewsRepository = ReviewsRepository
    private val userRepository = UserRepository

    private val _stationId = MutableStateFlow<Long?>(null)

    private val _translations = MutableStateFlow<Map<Review, String>>(emptyMap())
    val translations: StateFlow<Map<Review, String>> = _translations

    private val _translateError = MutableStateFlow<String?>(null)
    val translateError: StateFlow<String?> = _translateError

    val currentUser = userRepository.currentUser.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val station = _stationId.filterNotNull().flatMapLatest {
        stationRepository.getStationById(it)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val reviews = station.filterNotNull().map { it.reviews }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val bundles = station.filterNotNull().map { bundleChargers(it.chargers) }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val ratings = station.filterNotNull().map { calculateRatings(it) }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyMap()
    )

    val isFavorite =
        station.filterNotNull()
            .combine(favoritesRepository.userFavorites) { s, f -> f.contains(s.id) }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = false
            )

    fun deleteStation(onDelete: () -> Unit) {
        viewModelScope.launch {
            station.value?.let { st ->
                stationRepository.deleteStation(st)
                currentUser.value?.let {
                    favoritesRepository.removeFavorite(st.id, it.id)
                }
                _stationId.update { null }
                onDelete()
            }
        }
    }

    fun fetchStation(stationId: Long) {
        _stationId.update { stationId }
        ViewSyncService.syncStation(stationId)
    }

    fun toggleFavorite(onFail: () -> Unit) {
        viewModelScope.launch {
            currentUser.value?.let { user ->
                station.value?.let { station ->
                    try {
                        if (!isFavorite.value) {
                            favoritesRepository.addFavorite(station.id, user.id)
                        } else {
                            favoritesRepository.removeFavorite(station.id, user.id)
                        }
                    } catch (_: Exception) {
                        onFail()
                    }
                }
            }
        }
    }

    fun submitReview(comment: String, rating: Int, onFail: () -> Unit) {
        viewModelScope.launch {
            currentUser.value?.let { user ->
                station.value?.let { station ->
                    try {
                        val newReview = Review(
                            user = user,
                            comment = comment,
                            rating = rating,
                            date = Date()
                        )
                        reviewsRepository.addReview(newReview, station.id)
                        _translations.update { currentTranslations ->
                            currentTranslations - newReview
                        }
                    } catch (_: Exception) {
                        onFail()
                    }
                }
            }
        }
    }

    private fun calculateRatings(station: Station?): Map<Int, Int> {
        val base = mutableMapOf<Int, Int>().apply {
            for (i in 1..5)
                put(i, 0)
        }
        station?.reviews?.forEach {
            base[it.rating] = base.getOrDefault(it.rating, 0) + 1
        }
        return base
    }

    fun translate(review: Review, originalComment: String) {
        viewModelScope.launch {
            try {
                val targetLanguage = Locale.getDefault().language
                val req = TranslateRequest(text = originalComment, target = targetLanguage)
                val response = TranslationApi.translate(req).awaitResponse()
                if (response.isSuccessful) {
                    val translated = response.body()?.translatedText ?: ""
                    _translations.update { it + (review to translated) }
                } else {
                    _translateError.update { "Translation failed: ${response.message()}" }
                }
            } catch (e: Exception) {
                _translateError.update { e.localizedMessage ?: "Unknown error" }
            }
        }
    }

    fun clearTranslateError() {
        _translateError.update { null }
    }

    override fun onCleared() {
        ViewSyncService.stopStationSync()
    }
}
