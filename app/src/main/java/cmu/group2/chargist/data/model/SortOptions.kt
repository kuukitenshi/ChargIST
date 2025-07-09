package cmu.group2.chargist.data.model

import android.location.Location
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import cmu.group2.chargist.R
import cmu.group2.chargist.calculateDistanceMeters
import cmu.group2.chargist.estimateTravelTimeSeconds

// ---------------- enums ---------------
enum class SOptions {
    ASCENDANT_PRICE,
    DESCENDANT_PRICE,
    MORE_AVAILABLE,
    LESS_AVAILABLE,
    NEAREST,
    FARTHEST,
    FASTEST,
    SLOWEST,
    MORE_TIME_TRAVEL,
    LESS_TIME_TRAVEL
}

//------------------- sort class --------------
fun SOptions.toSortComparator(userLoc: Location?): Comparator<Station> {
    val userLocation = userLoc?.let { GeoLocation(it.latitude, it.longitude) }
    return Comparator { a, b ->
        if (userLocation == null && (this == SOptions.NEAREST || this == SOptions.FARTHEST || this == SOptions.FASTEST || this == SOptions.SLOWEST)) {
            0
        } else {
            when (this) {
                SOptions.ASCENDANT_PRICE -> {
                    val aPrice = a.chargers.minOfOrNull { it.price.toFloat() } ?: Float.MAX_VALUE
                    val bPrice = b.chargers.minOfOrNull { it.price.toFloat() } ?: Float.MAX_VALUE
                    aPrice.compareTo(bPrice)
                }

                SOptions.DESCENDANT_PRICE -> {
                    val aPrice = a.chargers.minOfOrNull { it.price.toFloat() } ?: 0f
                    val bPrice = b.chargers.minOfOrNull { it.price.toFloat() } ?: 0f
                    bPrice.compareTo(aPrice)
                }

                SOptions.MORE_AVAILABLE -> {
                    val aFree = a.chargers.any { it.status == ChargerStatus.FREE }
                    val bFree = b.chargers.any { it.status == ChargerStatus.FREE }
                    when {
                        aFree && !bFree -> 1
                        !aFree && bFree -> -1
                        else -> 0
                    }
                }

                SOptions.LESS_AVAILABLE -> {
                    val aFree = a.chargers.any { it.status == ChargerStatus.FREE }
                    val bFree = b.chargers.any { it.status == ChargerStatus.FREE }
                    when {
                        !aFree && bFree -> 1
                        aFree && !bFree -> -1
                        else -> 0
                    }
                }

                SOptions.NEAREST -> {
                    val distA = calculateDistanceMeters(userLocation!!, a.location)
                    val distB = calculateDistanceMeters(userLocation, b.location)
                    distA.compareTo(distB)
                }

                SOptions.FARTHEST -> {
                    val distA = calculateDistanceMeters(userLocation!!, a.location)
                    val distB = calculateDistanceMeters(userLocation, b.location)
                    distB.compareTo(distA)
                }

                SOptions.LESS_TIME_TRAVEL -> {
                    val timeA = estimateTravelTimeSeconds(
                        calculateDistanceMeters(
                            userLocation!!,
                            a.location
                        )
                    )
                    val timeB = estimateTravelTimeSeconds(
                        calculateDistanceMeters(
                            userLocation,
                            b.location
                        )
                    )
                    timeB.compareTo(timeA)
                }

                SOptions.MORE_TIME_TRAVEL -> {
                    val timeA = estimateTravelTimeSeconds(
                        calculateDistanceMeters(
                            userLocation!!,
                            a.location
                        )
                    )
                    val timeB = estimateTravelTimeSeconds(
                        calculateDistanceMeters(
                            userLocation,
                            b.location
                        )
                    )
                    timeA.compareTo(timeB)
                }

                SOptions.SLOWEST -> {
                    val aPower = a.chargers.maxOfOrNull { it.power } ?: ChargerPower.SLOW
                    val bPower = b.chargers.maxOfOrNull { it.power } ?: ChargerPower.SLOW
                    aPower.compareTo(bPower)
                }

                SOptions.FASTEST -> {
                    val aPower = a.chargers.maxOfOrNull { it.power } ?: ChargerPower.SLOW
                    val bPower = b.chargers.maxOfOrNull { it.power } ?: ChargerPower.SLOW
                    bPower.compareTo(aPower)
                }
            }
        }
    }
}

// ---------- utils funcs ----------------
@Composable
fun SOptions.toLocalizedName(): String = when (this) {
    SOptions.ASCENDANT_PRICE -> stringResource(R.string.price_sort)
    SOptions.DESCENDANT_PRICE -> stringResource(R.string.price_sort)
    SOptions.MORE_AVAILABLE -> stringResource(R.string.availability_sort)
    SOptions.LESS_AVAILABLE -> stringResource(R.string.availability_sort)
    SOptions.NEAREST -> stringResource(R.string.distance_sort)
    SOptions.FARTHEST -> stringResource(R.string.distance_sort)
    SOptions.FASTEST -> stringResource(R.string.power_sort)
    SOptions.SLOWEST -> stringResource(R.string.power_sort)
    SOptions.LESS_TIME_TRAVEL -> stringResource(R.string.travel_time_sort)
    SOptions.MORE_TIME_TRAVEL -> stringResource(R.string.travel_time_sort)
}

@Composable
fun SOptions.getArrowIcon(): ImageVector = when (this) {
    SOptions.ASCENDANT_PRICE, SOptions.MORE_AVAILABLE, SOptions.NEAREST, SOptions.FASTEST, SOptions.MORE_TIME_TRAVEL -> Icons.Default.ArrowUpward
    SOptions.DESCENDANT_PRICE, SOptions.LESS_AVAILABLE, SOptions.FARTHEST, SOptions.SLOWEST, SOptions.LESS_TIME_TRAVEL -> Icons.Default.ArrowDownward
}