package cmu.group2.chargist.ui.components.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Block
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun <T> ItemsRow(
    items: List<T>,
    modifier: Modifier = Modifier,
    alignment: Alignment.Horizontal = Alignment.Start,
    emptyMessageRes: Int,
    contentDescriptionRes: Int,
    getIcon: @Composable ((T) -> Painter),
    colorIcon: Color = Color.Unspecified,
    isNearby: Boolean = false
) {
    //--------- no methods -------------
    if (items.isEmpty()) {
        EmptyStateMessage(
            icon = Icons.Outlined.Block,
            message = stringResource(emptyMessageRes)
        )
    } else {
        // --------- list methods -------------------
        LazyRow(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(
                space = 12.dp,
                alignment = alignment
            ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items(
                items = items
            ) { item ->
                Image(
                    painter = getIcon(item),
                    contentDescription = stringResource(contentDescriptionRes),
                    modifier = Modifier
                        .size(if (isNearby) 30.dp else 50.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Fit,
                    colorFilter = if (isNearby) ColorFilter.tint(colorIcon) else null
                )
            }
        }
    }
}