package cmu.group2.chargist.ui.components.reviews

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cmu.group2.chargist.R

@Composable
fun RatingStarBar(rating: Int, onRatingChanged: (Int) -> Unit) {
    val starColor = Color(0xFFFFB300)
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        (1..5).forEach { star ->
            IconButton(onClick = { onRatingChanged(star) }) {
                Icon(
                    imageVector = if (star <= rating) Icons.Filled.Star else Icons.Outlined.Star,
                    contentDescription = stringResource(R.string.star_desc) + " $star",
                    tint = if (star <= rating) starColor else Color.Gray
                )
            }
        }
    }
}
