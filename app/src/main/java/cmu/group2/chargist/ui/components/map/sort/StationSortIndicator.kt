package cmu.group2.chargist.ui.components.map.sort

import android.annotation.SuppressLint
import android.location.Location
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.EvStation
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cmu.group2.chargist.calculateDistanceMeters
import cmu.group2.chargist.data.model.ChargerStatus
import cmu.group2.chargist.data.model.GeoLocation
import cmu.group2.chargist.data.model.SOptions
import cmu.group2.chargist.data.model.Station
import cmu.group2.chargist.estimateTravelTimeSeconds

@SuppressLint("DefaultLocale")
@Composable
fun StationSortIndicator(
    station: Station,
    selectedOption: SOptions? = null,
    userLocation: Location?,
) {
    val iconColor = MaterialTheme.colorScheme.primary
    val textStyle = MaterialTheme.typography.bodyMedium.copy(
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    val icon = when (selectedOption) {
        SOptions.MORE_AVAILABLE, SOptions.LESS_AVAILABLE -> Icons.Default.EvStation
        SOptions.NEAREST, SOptions.FARTHEST -> Icons.Default.LocationOn
        SOptions.FASTEST, SOptions.SLOWEST -> Icons.Default.Bolt
        SOptions.ASCENDANT_PRICE, SOptions.DESCENDANT_PRICE -> Icons.Default.AttachMoney
        SOptions.LESS_TIME_TRAVEL, SOptions.MORE_TIME_TRAVEL -> Icons.Default.Timer
        null -> Icons.Default.EvStation // default
    }

    val valueText = when (selectedOption) {
        SOptions.ASCENDANT_PRICE, SOptions.DESCENDANT_PRICE -> {
            val min = station.chargers.minOfOrNull { it.price }
            if (min == null || min == Double.MAX_VALUE) {
                "-"
            } else {
                String.format("%.2f €", min)
            }
        }

        SOptions.FASTEST, SOptions.SLOWEST -> {
            val power = station.chargers.maxOfOrNull { it.power } ?: ""
            "$power"
        }

        SOptions.NEAREST, SOptions.FARTHEST, SOptions.LESS_TIME_TRAVEL, SOptions.MORE_TIME_TRAVEL -> {
            val userGeo = userLocation?.let { GeoLocation(userLocation.latitude, it.longitude) }
            val dist = userLocation?.let {
                calculateDistanceMeters(
                    userGeo ?: GeoLocation(),
                    station.location
                ) / 1000f
            } ?: 0f

            if (selectedOption == SOptions.LESS_TIME_TRAVEL || selectedOption == SOptions.MORE_TIME_TRAVEL) {
                val time = estimateTravelTimeSeconds(dist) / 60f
                "%.2f min".format(time)
            } else {
                "%.2f km".format(dist)
            }
        }

        SOptions.MORE_AVAILABLE, SOptions.LESS_AVAILABLE -> {
            val available = station.chargers.count { it.status == ChargerStatus.FREE }
            "$available"
        }

        null -> "--"
    }

    SortIconWithText(
        icon = icon as ImageVector,
        text = valueText,
        iconColor = iconColor,
        textStyle = textStyle
    )
}

@Composable
private fun SortIconWithText(
    icon: ImageVector,
    text: String,
    iconColor: Color,
    textStyle: TextStyle
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = textStyle,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold
        )
    }
}