package cmu.group2.chargist.ui.components.reviews

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cmu.group2.chargist.R

@Composable
fun RatingHistogram(
    averageRating: Float,
    totalReviews: Int,
    ratingCounts: Map<Int, Int>
) {
    val starColor = Color(0xFFFFB300)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // ------------- avg stars ----------------
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "%.1f".format(averageRating),
                fontSize = 36.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row {
                repeat(5) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = if (it < averageRating.toInt()) starColor else Color.Gray
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$totalReviews " + stringResource(R.string.reviews),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(2.dp))

        // ---------- histogram --------------------
        Column(modifier = Modifier.weight(1f)) {
            for (i in 5 downTo 1) {
                val count = ratingCounts[i] ?: 0
                RatingBarRow(rating = i, count = count, total = totalReviews)
            }
        }
    }
}

@Composable
fun RatingBarRow(rating: Int, count: Int, total: Int) {

    val progress = if (total > 0) count / total.toFloat() else 0f
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Text(
            text = "$rating",
            modifier = Modifier.width(12.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        //--------- histogram bar ----------
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier
                .width(170.dp)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

