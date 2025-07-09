package cmu.group2.chargist.ui.components.station

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cmu.group2.chargist.R
import cmu.group2.chargist.data.model.NearbyService
import cmu.group2.chargist.data.model.getIcon
import cmu.group2.chargist.ui.components.common.ItemsRow

@Composable
fun NearbyServicesRow(
    services: List<NearbyService>,
    alignment: Alignment.Horizontal = Alignment.Start,
    colorIcon: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    ItemsRow(
        items = services,
        alignment = alignment,
        emptyMessageRes = R.string.no_nearby_services_available,
        contentDescriptionRes = R.string.nearby_service_desc,
        getIcon = { it.getIcon() },
        colorIcon = colorIcon,
        isNearby = true,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
    )
}
