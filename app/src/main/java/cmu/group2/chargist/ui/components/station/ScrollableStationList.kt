package cmu.group2.chargist.ui.components.station

import android.location.Location
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cmu.group2.chargist.data.model.SOptions
import cmu.group2.chargist.data.model.Station

@Composable
fun ScrollableStationList(
    stations: List<Station>,
    onStationClick: (Station) -> Unit,
    isFavorite: Boolean = true,
    onNavigateToLocation: (Double, Double) -> Unit,
    selectedOption: SOptions? = null,
    userLocation: Location? = null,
) {
    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        items(stations) { station ->
            StationCard(
                station = station,
                onClick = { onStationClick(station) },
                isFavorite = isFavorite,
                onNavigateToLocation = onNavigateToLocation,
                selectedOption = selectedOption,
                userLocation = userLocation,
            )
        }
    }
}