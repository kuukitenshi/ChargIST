package cmu.group2.chargist.ui.components.map.filter

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cmu.group2.chargist.data.model.NearbyService
import cmu.group2.chargist.data.model.getIcon
import cmu.group2.chargist.data.model.toLocalizedName

@Composable
fun NearbyServicesButton(
    service: NearbyService,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SelectableCircularButton(
        icon = service.getIcon(),
        label = service.toLocalizedName(),
        isSelected = isSelected,
        onClick = onClick,
        modifier = modifier,
        iconSize = 16.dp,
        circleSize = 32.dp,
        textSize = MaterialTheme.typography.labelSmall.fontSize,
    )
}
