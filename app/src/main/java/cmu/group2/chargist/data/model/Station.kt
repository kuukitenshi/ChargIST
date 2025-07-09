package cmu.group2.chargist.data.model

import androidx.annotation.DrawableRes
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import cmu.group2.chargist.R

enum class PaymentMethod {
    CASH,
    MB_WAY,
    GOOGLE_PAY,
    VISA,
    MASTERCARD
}

enum class NearbyService {
    COFFEE_SHOP,
    RESTAURANT,
    WC,
    CAR_WASH,
    GAS_STATION,
    MOBILITY_RENTAL,
    SUPERMARKET,
    ATM,
    PHARMACY,
    WIFI_ZONE,
    PLAYGROUND,
    LIBRARY
}

data class Station(
    val id: Long,
    val name: String,
    val location: GeoLocation,
    val imageUrl: String?,
    val chargers: List<Charger>,
    val avgRating: Float,
    val paymentMethods: List<PaymentMethod>,
    val reviews: List<Review> = emptyList(),
    val nearbyServices: List<NearbyService>,
)

// ----------------- utils func ------------------------------
@Composable
fun NearbyService.toLocalizedName(): String = when (this) {
    NearbyService.COFFEE_SHOP -> stringResource(R.string.coffee_shop)
    NearbyService.RESTAURANT -> stringResource(R.string.restaurant)
    NearbyService.WC -> stringResource(R.string.bathroom)
    NearbyService.CAR_WASH -> stringResource(R.string.car_wash)
    NearbyService.GAS_STATION -> stringResource(R.string.gas_station)
    NearbyService.MOBILITY_RENTAL -> stringResource(R.string.mobility_rental)
    NearbyService.SUPERMARKET -> stringResource(R.string.supermarket)
    NearbyService.ATM -> stringResource(R.string.atm)
    NearbyService.PHARMACY -> stringResource(R.string.pharmacy)
    NearbyService.WIFI_ZONE -> stringResource(R.string.wifi_zone)
    NearbyService.PLAYGROUND -> stringResource(R.string.playground)
    NearbyService.LIBRARY -> stringResource(R.string.library)
}

@Composable
fun NearbyService.getIcon(): Painter = when (this) {
    NearbyService.COFFEE_SHOP -> painterResource(R.drawable.ic_coffee_shop)
    NearbyService.RESTAURANT -> painterResource(R.drawable.ic_restaurant)
    NearbyService.WC -> painterResource(R.drawable.ic_wc)
    NearbyService.CAR_WASH -> painterResource(R.drawable.ic_car_wash)
    NearbyService.GAS_STATION -> painterResource(R.drawable.ic_gas_station)
    NearbyService.MOBILITY_RENTAL -> painterResource(R.drawable.ic_rental)
    NearbyService.SUPERMARKET -> painterResource(R.drawable.ic_supermarket)
    NearbyService.ATM -> painterResource(R.drawable.ic_atm)
    NearbyService.PHARMACY -> painterResource(R.drawable.ic_pharmacy)
    NearbyService.WIFI_ZONE -> painterResource(R.drawable.ic_wifi)
    NearbyService.PLAYGROUND -> painterResource(R.drawable.ic_playground)
    NearbyService.LIBRARY -> painterResource(R.drawable.ic_library)
}

@Composable
fun PaymentMethod.getIcon(): Painter {
    val isDark = isSystemInDarkTheme()
    @DrawableRes val resId = when (this) {
        PaymentMethod.CASH -> R.drawable.money_icon
        PaymentMethod.MB_WAY -> if (isDark) R.drawable.mbway_dark else R.drawable.mbway_icon
        PaymentMethod.GOOGLE_PAY -> R.drawable.google_play_icon
        PaymentMethod.VISA -> R.drawable.visa_icon
        PaymentMethod.MASTERCARD -> R.drawable.mc_icon
    }
    return painterResource(id = resId)
}