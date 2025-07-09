package cmu.group2.chargist.ui.components.map.filter

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp

@Composable
fun SelectableCircularButton(
    icon: Painter,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconSize: Dp = 36.dp,
    circleSize: Dp = 64.dp,
    textSize: TextUnit = MaterialTheme.typography.bodyMedium.fontSize,
    selectedBackgroundColor: Color = Color(0xFF5ca462).copy(alpha = 0.3f),
    selectedContentColor: Color = Color(0xFF5ca462),
) {
    val backgroundColor =
        if (isSelected) selectedBackgroundColor else Color.LightGray.copy(alpha = 0.3f)
    val textColor =
        if (isSelected) selectedContentColor else MaterialTheme.colorScheme.onSurfaceVariant
    val iconColorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .padding(2.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(circleSize)
                .clip(CircleShape)
                .background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = icon,
                contentDescription = label,
                modifier = Modifier.size(iconSize),
                colorFilter = iconColorFilter
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = textColor,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            fontSize = textSize,
            maxLines = 1,
            textAlign = TextAlign.Center
        )
    }
}