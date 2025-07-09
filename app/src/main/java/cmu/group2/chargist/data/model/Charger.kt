package cmu.group2.chargist.data.model

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import cmu.group2.chargist.R

enum class ChargerType {
    CCS2,
    TYPE2
}

enum class ChargerPower {
    FAST,
    MEDIUM,
    SLOW;
}

enum class ChargerIssue {
    FINE,
    VANDALIZED,
    NOT_CHARGING,
    DAMAGED;
}

enum class ChargerStatus {
    FREE,
    OCCUPIED,
    BROKEN;
}

data class Charger(
    val id: Long,
    val type: ChargerType,
    val power: ChargerPower,
    val price: Double,
    val issue: ChargerIssue = ChargerIssue.FINE,
    val status: ChargerStatus = ChargerStatus.FREE
)

data class ChargerBundle(
    val type: ChargerType,
    val power: ChargerPower,
    val price: Double,
    val amount: Int,
    val available: Int,
    val chargers: List<Charger>
)

// ------- utility functions--------

@Composable
fun ChargerPower.toLocalizedName(): String = when (this) {
    ChargerPower.FAST -> stringResource(R.string.fast)
    ChargerPower.MEDIUM -> stringResource(R.string.medium)
    ChargerPower.SLOW -> stringResource(R.string.slow)
}

@Composable
fun ChargerPower.toColor(): Color = when (this) {
    ChargerPower.FAST -> Color(0xFF4CAF50)
    ChargerPower.MEDIUM -> Color(0xFFFFC107)
    ChargerPower.SLOW -> Color(0xFFF44336)
}

@Composable
fun ChargerIssue.toLocalizedName(): String = when (this) {
    ChargerIssue.FINE -> stringResource(R.string.fine)
    ChargerIssue.VANDALIZED -> stringResource(R.string.vandalized)
    ChargerIssue.NOT_CHARGING -> stringResource(R.string.not_charging)
    ChargerIssue.DAMAGED -> stringResource(R.string.damaged)
}

@Composable
fun ChargerStatus.toLocalizedName(): String = when (this) {
    ChargerStatus.FREE -> stringResource(R.string.free)
    ChargerStatus.OCCUPIED -> stringResource(R.string.occupied)
    ChargerStatus.BROKEN -> stringResource(R.string.broken)
}

@Composable
fun ChargerType.getIcon(): Painter = when (this) {
    ChargerType.CCS2 -> painterResource(R.drawable.ev_plug_ccs2)
    ChargerType.TYPE2 -> painterResource(R.drawable.ev_plug_type2)
}
