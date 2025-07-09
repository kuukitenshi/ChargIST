package cmu.group2.chargist

import android.content.Context
import android.location.Location
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import androidx.navigation.NavController
import cmu.group2.chargist.data.model.Charger
import cmu.group2.chargist.data.model.ChargerBundle
import cmu.group2.chargist.data.model.ChargerIssue
import cmu.group2.chargist.data.model.ChargerStatus
import cmu.group2.chargist.data.model.GeoLocation
import com.lambdapioneer.argon2kt.Argon2Kt
import com.lambdapioneer.argon2kt.Argon2Mode
import java.io.File
import java.security.SecureRandom

private val argon2kt = Argon2Kt()
private val rand = SecureRandom()

fun bundleChargers(
    chargers: List<Charger>,
    filter: (Charger) -> Boolean = { true }
): List<ChargerBundle> {
    return chargers
        .filter(filter)
        .groupBy { Triple(it.type, it.power, it.price) }
        .map { (key, group) ->
            val (type, power, price) = key
            val amount = group.size
            val available =
                group.count { it.status == ChargerStatus.FREE && it.issue == ChargerIssue.FINE }
            ChargerBundle(
                type = type,
                power = power,
                price = price,
                amount = amount,
                available = available,
                chargers = group
            )
        }
}

fun NavController.popAll() {
    var pop: Boolean
    do {
        pop = popBackStack()
    } while (pop)
}

fun Context.createTmpFile(suffix: String): File {
    val filename = "file_${System.nanoTime()}"
    return File.createTempFile(filename, suffix, externalCacheDir)
}

fun Context.isOnMeteredConnection(): Boolean {
    val conn = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    if (conn.activeNetwork != null) {
        val capabilities = conn.getNetworkCapabilities(conn.activeNetwork)
        return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED) == false
    }
    return true
    return conn.activeNetwork?.let {
        val capabilities = conn.getNetworkCapabilities(it)
        capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED) == true
    } == true
}

fun Context.readUriBytes(uri: Uri?): ByteArray? {
    return uri?.let {
        contentResolver.openInputStream(it).use {
            it?.readAllBytes()
        }
    }
}

fun hashPassword(password: String): String {
    val salt = ByteArray(16)
    rand.nextBytes(salt)
    val hash = argon2kt.hash(Argon2Mode.ARGON2_I, password.toByteArray(Charsets.UTF_8), salt)
    return hash.encodedOutputAsString()
}

fun verifyPassword(password: String, hash: String): Boolean {
    return argon2kt.verify(
        mode = Argon2Mode.ARGON2_I,
        encoded = hash,
        password = password.toByteArray(Charsets.UTF_8)
    )
}

//-------------- dist + tt ------------------
fun calculateDistanceMeters(loc1: GeoLocation, loc2: GeoLocation): Float {
    val result = FloatArray(1)
    Location.distanceBetween(loc1.latitude, loc1.longitude, loc2.latitude, loc2.longitude, result)
    return result[0]
}

fun estimateTravelTimeSeconds(distanceMeters: Float, speedMetersPerSecond: Float = 8.33f): Float {
    return distanceMeters / speedMetersPerSecond // 30kms/h -> 8.33m/s
}