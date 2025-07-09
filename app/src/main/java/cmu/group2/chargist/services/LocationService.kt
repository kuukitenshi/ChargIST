package cmu.group2.chargist.services

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class LocationService : AbstractCoroutineService() {

    private lateinit var fusedLocation: FusedLocationProviderClient
    private var started = false

    companion object {
        private val _currentLocation = MutableStateFlow<Location?>(null)
        val currentLocation = _currentLocation.asStateFlow()
    }

    private object LocationHandler : LocationCallback() {

        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            Log.d("LocationService", "Updating location")
            _currentLocation.update { result.lastLocation }
        }
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocation = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!started) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                startLocationTracking()
                started = true
            }
        }
        return START_STICKY
    }

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun startLocationTracking() {
        Log.d("LocationService", "Starting location tracking")
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000).build()
        fusedLocation.requestLocationUpdates(request, LocationHandler, Looper.getMainLooper())
        fusedLocation.lastLocation.addOnSuccessListener {
            Log.d("LocationService", "Updating current location")
            _currentLocation.update { it }
        }
    }
}