package cmu.group2.chargist.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cmu.group2.chargist.data.model.User
import cmu.group2.chargist.data.repository.FavoritesRepository
import cmu.group2.chargist.data.repository.StationRepository
import cmu.group2.chargist.data.repository.UserRepository
import cmu.group2.chargist.readUriBytes
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {
    private val userRepository = UserRepository
    private val favoritesRepository = FavoritesRepository
    private val stationRepository = StationRepository

    val currentUser: StateFlow<User?> = userRepository.currentUser.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val favStations =
        stationRepository.stations.combine(favoritesRepository.userFavorites) { stations, favorites ->
            stations.filter { favorites.contains(it.id) }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun updateName(newName: String, onFail: () -> Unit) {
        viewModelScope.launch {
            currentUser.value?.let {
                try {
                    userRepository.changeName(it, newName)
                } catch (_: Exception) {
                    onFail()
                }
            }
        }
    }

    fun updateProfileImage(context: Context, imageUri: Uri?, onFail: () -> Unit) {
        viewModelScope.launch {
            val bytes = context.readUriBytes(imageUri)
            try {
                currentUser.value?.let {
                    userRepository.changePicture(it, bytes)
                }
            } catch (_: Exception) {
                onFail()
            }
        }
    }

    fun logout() {
        userRepository.logout()
    }
}