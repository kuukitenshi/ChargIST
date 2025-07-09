package cmu.group2.chargist.ui.components.station.form

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cmu.group2.chargist.R

@Composable
fun TrashToggleButton(
    isActive: Boolean,
    onToggle: () -> Unit
) {
    IconButton(
        onClick = onToggle,
        modifier = Modifier
            .size(48.dp)
            .background(
                color = if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                shape = CircleShape
            )
    ) {
        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = stringResource(R.string.delete_mode),
            tint = if (isActive) MaterialTheme.colorScheme.primary else Color.Gray
        )
    }
}
