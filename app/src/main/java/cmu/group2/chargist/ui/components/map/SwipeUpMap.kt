package cmu.group2.chargist.ui.components.map

import android.location.Location
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EvStation
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cmu.group2.chargist.R
import cmu.group2.chargist.data.api.GeoResponse
import cmu.group2.chargist.data.model.SOptions
import cmu.group2.chargist.data.model.Station
import cmu.group2.chargist.data.model.toLocalizedName
import cmu.group2.chargist.ui.components.map.filter.FilterButton
import cmu.group2.chargist.ui.components.map.sort.Dropdown
import cmu.group2.chargist.ui.components.station.StationCard
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeUpMap(
    isVisible: Boolean,
    sheetState: SheetState,
    coroutineScope: CoroutineScope,
    onDismiss: () -> Unit,
    searchQuery: String,
    searchResults: List<GeoResponse>,
    onSearchChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    stations: List<Station>,
    selectedSortOption: SOptions,
    onStationClick: (Station) -> Unit,
    onFilterClick: () -> Unit,
    onNavigateToLocation: (Double, Double) -> Unit,
    onApplySortOption: (SOptions) -> Unit,
    userLocation: Location?,
    listState: LazyListState = rememberLazyListState(),
    onMoreClick: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }

    if (isVisible) {
        ModalBottomSheet(
            onDismissRequest = {
                onDismiss()
                coroutineScope.launch { sheetState.hide() }
            },
            sheetState = sheetState,
            modifier = Modifier.fillMaxHeight()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // ---------------- search -------------------
                SearchBarWithClear(
                    searchQuery = searchQuery,
                    searchResults = searchResults,
                    stationsList = stations,
                    onSearchChange = onSearchChange,
                    onClearSearch = onClearSearch,
                    focusRequester = focusRequester,
                    autoFocus = true,
                    onNavigateToLocation = onNavigateToLocation
                )
                Spacer(modifier = Modifier.height(16.dp))

                // --------------- title + filter btn -----------------
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.charger_stations),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    FilterButton(onClick = { onFilterClick() })
                }
                Spacer(modifier = Modifier.height(2.dp))

                //----------------- sort ------------------------------
                Dropdown(
                    label = stringResource(R.string.sort),
                    selectedOption = selectedSortOption,
                    options = SOptions.entries.toList(),
                    onOptionSelected = onApplySortOption,
                    optionLabel = { it.toLocalizedName() }
                )
                Spacer(modifier = Modifier.height(8.dp))

                // ----------------- available stations ---------------
                if (stations.isNotEmpty()) {
                    LazyColumn(modifier = Modifier.fillMaxWidth(), state = listState) {
                        items(stations) { station ->
                            StationCard(
                                station = station,
                                onClick = { onStationClick(station) },
                                isFavorite = false,
                                onNavigateToLocation = onNavigateToLocation,
                                selectedOption = selectedSortOption,
                                userLocation = userLocation,
                            )
                        }
                        item {
                            // --------- load more -------
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                TextButton(onClick = { onMoreClick() }) {
                                    Text(text = stringResource(R.string.load_more))
                                }
                            }
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.EvStation,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            modifier = Modifier
                                .height(60.dp)
                                .padding(bottom = 12.dp)
                        )
                        Text(
                            text = stringResource(R.string.no_stations_with_available_chargers),
                            textAlign = TextAlign.Center,
                            fontSize = 16.sp,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        )
                        TextButton(onClick = { onMoreClick() }) { Text("Load more") }
                    }
                }
            }
        }
    }
}

