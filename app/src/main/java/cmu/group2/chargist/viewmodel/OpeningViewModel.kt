package cmu.group2.chargist.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cmu.group2.chargist.data.model.GuestUser
import cmu.group2.chargist.data.repository.UserRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class OpeningViewModel : ViewModel() {
    private val userRepository = UserRepository

    val currentUser = userRepository.currentUser.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    fun continueAsGuest() {
        viewModelScope.launch {
            userRepository.setLoggedInUser(GuestUser)
        }
    }
}