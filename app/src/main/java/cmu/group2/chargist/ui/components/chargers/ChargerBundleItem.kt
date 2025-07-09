package cmu.group2.chargist.ui.components.chargers

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import cmu.group2.chargist.data.model.Charger
import cmu.group2.chargist.data.model.ChargerIssue
import cmu.group2.chargist.data.model.ChargerStatus
import cmu.group2.chargist.data.model.getIcon
import cmu.group2.chargist.data.model.toColor
import cmu.group2.chargist.data.model.toLocalizedName

@Composable
fun ChargerBundleItem(
    charger: Charger,
    onToggleStatus: (ChargerStatus) -> Unit,
    onReportIssue: (ChargerIssue) -> Unit,
    onRepair: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    // --------- tag color -------------
    val tagColor =
        if (charger.status == ChargerStatus.FREE) Color(0xFF4CAF50) else Color(0xFFF44336)
    val tagBackgroundColor = tagColor.copy(alpha = 0.1f)
    val errorColor = Color(0xFFC93E48)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            // ----------- img ---------------------
            Image(
                painter = charger.type.getIcon(),
                contentDescription = charger.type.name,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp)),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant)
            )
            Spacer(Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                // ----------- type card ---------------------
                Text(
                    text = charger.type.name,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 18.sp
                )
                Spacer(Modifier.height(4.dp))

                // ----------- power ---------------------
                Text(
                    text = charger.power.toLocalizedName(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = charger.power.toColor(),
                        shadow = Shadow(
                            color = Color.Black.copy(alpha = 0.4f),
                            offset = Offset(1f, 1f),
                            blurRadius = 1f
                        )
                    ),
                    modifier = Modifier
                        .background(
                            charger.power.toColor().copy(alpha = 0.1f),
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )

                //---------- issue ---------------
                if (charger.status == ChargerStatus.BROKEN) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Issue: ${charger.issue.toLocalizedName()}",
                        color = errorColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(Modifier.width(8.dp))

            // ----------- free/occupied btn ---------------------
            if (charger.status != ChargerStatus.BROKEN) {
                Button(
                    onClick = {
                        val newStatus = when (charger.status) {
                            ChargerStatus.FREE -> ChargerStatus.OCCUPIED
                            ChargerStatus.OCCUPIED -> ChargerStatus.FREE
                            else -> charger.status
                        }
                        onToggleStatus(newStatus)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = tagBackgroundColor,
                        contentColor = tagColor,
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(40.dp)
                ) {
                    Text(text = charger.status.toLocalizedName())
                }
                Spacer(Modifier.width(8.dp))
            }
            //--------------- issue btn -----------
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFFFB300))
                    .clickable { showDialog = true }
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = stringResource(R.string.report_broken),
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }

    // ------------ btn issue popup --------------
    IssuePopUp(
        showDialog = showDialog,
        onDismiss = { showDialog = false },
        onConfirm = { issue -> onReportIssue(issue) },
        onRepair = onRepair,
        chargerIssue = charger.issue
    )
}
