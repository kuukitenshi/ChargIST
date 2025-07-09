package cmu.group2.chargist.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cmu.group2.chargist.data.api.TranslateRequest
import cmu.group2.chargist.data.api.TranslationApi
import cmu.group2.chargist.data.model.Review
import cmu.group2.chargist.data.repository.ReviewsRepository
import cmu.group2.chargist.data.repository.StationRepository
import cmu.group2.chargist.data.repository.UserRepository
import cmu.group2.chargist.services.ViewSyncService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.awaitResponse
import java.util.Date
import java.util.Locale

class ReviewViewModel : ViewModel() {
    private val stationRepository = StationRepository
    private val userRepository = UserRepository
    private val reviewsRepository = ReviewsRepository

    private val _translations = MutableStateFlow<Map<Review, String>>(emptyMap())
    val translations: StateFlow<Map<Review, String>> = _translations

    private val _translateError = MutableStateFlow<String?>(null)
    val translateError: StateFlow<String?> = _translateError

    val currentUser = userRepository.currentUser.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    private val _stationId = MutableStateFlow<Long?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val station = _stationId.filterNotNull().flatMapLatest {
        stationRepository.getStationById(it)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    fun fetchStation(stationId: Long) {
        _stationId.update { stationId }
    }

    fun updateFetchReviews(stationId: Long, nextOffset: Long) {
        Log.d("ReviewSync", "fetch vm: next vm offset $nextOffset")
        ViewSyncService.loadPageReviews(stationId, nextOffset)
    }

    fun getRatingFrequency(): Map<Int, Int> {
        val base = mutableMapOf<Int, Int>().apply {
            (1..5).forEach {
                put(it, 0)
            }
        }
        station.value?.reviews?.forEach {
            base[it.rating] = base.getOrDefault(it.rating, 0) + 1
        }
        return base
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
}
