package cmu.group2.chargist.ui.components.station.form

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cmu.group2.chargist.data.model.ChargerPower
import cmu.group2.chargist.data.model.toColor
import cmu.group2.chargist.data.model.toLocalizedName

@Composable
fun PowerTagButton(
    power: ChargerPower,
    selected: Boolean,
    onClick: () -> Unit
) {
    val color = power.toColor()
    val bgColor = if (selected) color.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
    val borderColor = if (selected) color else MaterialTheme.colorScheme.outline
    val textColor = if (selected) color else MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = Modifier
            .padding(4.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text = power.toLocalizedName(), color = textColor, fontWeight = FontWeight.Medium)
    }
}
