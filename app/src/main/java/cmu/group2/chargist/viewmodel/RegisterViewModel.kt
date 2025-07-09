package cmu.group2.chargist.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cmu.group2.chargist.data.model.UserAlreadyExistsException
import cmu.group2.chargist.data.repository.FavoritesRepository
import cmu.group2.chargist.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private val PASSWORD_PATTERN = "^(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{8,}$".toRegex()

class RegisterViewModel : ViewModel() {
    private val userRepository: UserRepository = UserRepository
    private val favoritesRepository = FavoritesRepository

    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val registerState: StateFlow<RegisterState> = _registerState

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password

    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword: StateFlow<String> = _confirmPassword

    private val _isPasswordValid = MutableStateFlow(false)
    val isPasswordValid: StateFlow<Boolean> = _isPasswordValid

    private val _doPasswordsMatch = MutableStateFlow(false)
    val doPasswordsMatch: StateFlow<Boolean> = _doPasswordsMatch

    val currentUser = userRepository.currentUser.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val favorites = favoritesRepository.userFavorites.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        viewModelScope.launch {
            password.collect { pwd -> _isPasswordValid.update { validatePassword(pwd) } }
        }
        viewModelScope.launch {
            combine(password, confirmPassword) { pwd, confirmPwd ->
                pwd == confirmPwd
            }.collect { match -> _doPasswordsMatch.update { match } }
        }
    }

    fun register() {
        viewModelScope.launch {
            _registerState.update { RegisterState.Loading }
            try {
                userRepository.register(
                    username.value,
                    name.value,
                    password.value,
                    favorites.value
                )
                _registerState.update { RegisterState.Success }
            } catch (e: UserAlreadyExistsException) {
                _registerState.update { RegisterState.Error(RegisterErrorType.USER_ALREADY_EXISTS) }
                e.printStackTrace()
            } catch (e: Exception) {
                _registerState.update { RegisterState.Error(RegisterErrorType.UNKNOWN) }
                e.printStackTrace()
            }
        }
    }

    fun updateName(name: String) = _name.update { name }

    fun updateUsername(username: String) = _username.update { username }

    fun updatePassword(password: String) = _password.update { password }

    fun updateConfirmPassword(password: String) = _confirmPassword.update { password }

    fun isFormValid() = username.value.isNotBlank() &&
            name.value.isNotBlank() &&
            password.value.isNotBlank() &&
            confirmPassword.value.isNotBlank() &&
            isPasswordValid.value &&
            doPasswordsMatch.value &&
            registerState.value !is RegisterState.Loading &&
            registerState.value !is RegisterState.Success

    private fun validatePassword(pwd: String): Boolean {
        return pwd.matches(PASSWORD_PATTERN)
    }
}

sealed class RegisterState {
    data object Idle : RegisterState()
    data object Loading : RegisterState()
    data object Success : RegisterState()
    data class Error(val type: RegisterErrorType) : RegisterState()
}

enum class RegisterErrorType {
    USER_ALREADY_EXISTS,
    UNKNOWN
}