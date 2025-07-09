package cmu.group2.chargist.ui.components.chargers

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cmu.group2.chargist.R
import cmu.group2.chargist.data.model.ChargerBundle
import cmu.group2.chargist.data.model.getIcon
import cmu.group2.chargist.data.model.toColor
import cmu.group2.chargist.data.model.toLocalizedName

@Composable
fun ChargerBundleCard(
    chargerBundle: ChargerBundle,
    onClick: (ChargerBundle) -> Unit,
    modifier: Modifier = Modifier,
    showCheckCircle: Boolean = false,
    isChecked: Boolean = false
) {
    val availableAmount = chargerBundle.available
    val tagColor = chargerBundle.power.toColor().copy(alpha = 0.8f)
    val tagBackgroundColor = tagColor.copy(alpha = 0.1f)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .clickable { onClick.invoke(chargerBundle) },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // ---------- check remove circle ----------------
            if (showCheckCircle) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(if (isChecked) MaterialTheme.colorScheme.primary else Color.LightGray)
                        .padding(4.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            // ---------- image -----------------------
            Image(
                painter = chargerBundle.type.getIcon(),
                contentDescription = chargerBundle.type.name,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp)),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant)
            )
            Spacer(Modifier.width(8.dp))

            // ------------- body ----------------------
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = chargerBundle.type.name,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleSmall,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    // ------- tag ---------------
                    Text(
                        text = chargerBundle.power.toLocalizedName(),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = tagColor,
                            shadow = Shadow(
                                color = Color.Black.copy(alpha = 0.4f),
                                offset = Offset(1f, 1f),
                                blurRadius = 1f
                            )
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(tagBackgroundColor)
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        fontSize = 12.sp
                    )
                }
                Spacer(Modifier.height(20.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // ----------- availability ------------
                    Text(
                        text = "${availableAmount}/${chargerBundle.amount} ${stringResource(R.string.available)}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    // ----------- price ------------
                    Text(
                        text = "%.2f€".format(chargerBundle.price),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
