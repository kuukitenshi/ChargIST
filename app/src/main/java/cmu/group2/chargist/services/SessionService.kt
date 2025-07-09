package cmu.group2.chargist.services

import android.content.SharedPreferences
import android.widget.Toast
import androidx.core.content.edit
import cmu.group2.chargist.R
import cmu.group2.chargist.data.repository.UserRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class SessionService : AbstractCoroutineService() {
    private val userRepository = UserRepository

    private val currentUser = userRepository.currentUser.stateIn(
        scope = serviceScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    override fun onCreate() {
        super.onCreate()
        val sharedPreferences = application.getSharedPreferences("session", MODE_PRIVATE)
        refreshSession(sharedPreferences)
        serviceScope.launch {
            currentUser.collect {
                it?.let {
                    sharedPreferences.edit {
                        putString("userId", it.id.toString())
                    }
                } ?: sharedPreferences.edit {
                    remove("userId")
                }
            }
        }
    }

    private fun refreshSession(sharedPreferences: SharedPreferences) {
        val userIdString = sharedPreferences.getString("userId", "")
        val errorLogin = application.getString(R.string.error_refresh_session)
        runBlocking {
            if (!userIdString.isNullOrBlank()) {
                val userId = userIdString.toLong()
                try {
                    userRepository.refreshUserSession(userId)
                } catch (_: Exception) {
                    Toast.makeText(application.applicationContext, errorLogin, Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

}