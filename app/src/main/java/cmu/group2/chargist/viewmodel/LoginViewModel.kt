package cmu.group2.chargist.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cmu.group2.chargist.data.model.UserNotFoundException
import cmu.group2.chargist.data.model.WrongPasswordException
import cmu.group2.chargist.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    private val userRepository: UserRepository = UserRepository

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password

    fun login() {
        viewModelScope.launch {
            _loginState.update { LoginState.Loading }
            try {
                userRepository.login(username.value, password.value)
                _loginState.update { LoginState.Success }
            } catch (_: WrongPasswordException) {
                _loginState.update { LoginState.Error(LoginErrorType.WRONG_PASSWORD) }
            } catch (_: UserNotFoundException) {
                _loginState.update { LoginState.Error(LoginErrorType.USER_NOT_FOUND) }
            } catch (_: Exception) {
                _loginState.update { LoginState.Error(LoginErrorType.UNKNOWN) }
            }
        }
    }

    fun updateUsername(username: String) = _username.update { username }

    fun updatePassword(password: String) = _password.update { password }

    fun isFormValid() = username.value.isNotBlank() &&
            password.value.isNotBlank() &&
            loginState.value !is LoginState.Loading &&
            loginState.value !is LoginState.Success
}


sealed class LoginState {
    data object Idle : LoginState()
    data object Loading : LoginState()
    data object Success : LoginState()
    data class Error(val type: LoginErrorType) : LoginState()
}

enum class LoginErrorType {
    USER_NOT_FOUND,
    WRONG_PASSWORD,
    UNKNOWN
}