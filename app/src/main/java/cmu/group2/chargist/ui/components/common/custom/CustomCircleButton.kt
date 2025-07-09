package cmu.group2.chargist.ui.components.common.custom

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun CustomCircleButton(
    icon: ImageVector,
    description: String,
    onClick: () -> Unit
) {
    // circle button with an icon
    IconButton(
        modifier = Modifier
            .padding(6.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
            .width(48.dp)
            .height(48.dp),
        onClick = onClick
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = MaterialTheme.colorScheme.onPrimary
        )
    }
}
