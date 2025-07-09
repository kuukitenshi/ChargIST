package cmu.group2.chargist.ui.components.map.filter

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cmu.group2.chargist.data.model.ChargerType
import cmu.group2.chargist.data.model.getIcon

@Composable
fun TypeChargerButton(
    type: ChargerType,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SelectableCircularButton(
        icon = type.getIcon(),
        label = type.name,
        isSelected = isSelected,
        onClick = onClick,
        modifier = modifier
    )
}

