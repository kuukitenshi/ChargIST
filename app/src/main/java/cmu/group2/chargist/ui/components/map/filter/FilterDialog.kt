package cmu.group2.chargist.ui.components.map.filter

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import cmu.group2.chargist.R
import cmu.group2.chargist.data.model.ChargerPower
import cmu.group2.chargist.data.model.ChargerType
import cmu.group2.chargist.data.model.FilterOptions
import cmu.group2.chargist.data.model.NearbyService
import cmu.group2.chargist.ui.components.common.BackArrow
import cmu.group2.chargist.ui.components.common.custom.CustomMainTitle
import cmu.group2.chargist.ui.components.station.PaymentMethodsSelection
import cmu.group2.chargist.ui.components.station.form.PowerTagButton

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilterDialog(
    onDismiss: () -> Unit,
    filterOptions: FilterOptions,
    onApply: () -> Unit,
    onClearFilters: () -> Unit
) {
    val onlyAvailable by filterOptions.onlyAvailable.collectAsState()
    val selectedTypes by filterOptions.selectedTypes.collectAsState()
    val selectedPowers by filterOptions.selectedPowers.collectAsState()
    val priceRange by filterOptions.priceRange.collectAsState()
    val selectedPayments by filterOptions.selectedPayments.collectAsState()
    val selectedServices by filterOptions.selectedServices.collectAsState()
    val maxDistance by filterOptions.maxDistance.collectAsState()
    val maxTravelTime by filterOptions.maxTravelTime.collectAsState()

    Dialog(
        onDismissRequest = { onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxHeight(0.94f)
                .fillMaxWidth()
                .fillMaxSize()
                .background(Color.White)
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { CustomMainTitle(title = stringResource(R.string.filter)) },
                        navigationIcon = { BackArrow(onBackClick = { onDismiss() }) },
                        actions = {
                            TextButton(onClick = { onClearFilters() }) {
                                Text(text = stringResource(R.string.clear_filters))
                            }
                        }
                    )
                },
                bottomBar = {
                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        onClick = onApply
                    ) {
                        Text(text = stringResource(R.string.apply_filters))
                    }
                },
                containerColor = Color.White
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    // ---------------------------- only available ----------------------------
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            stringResource(R.string.only_stations_available),
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Switch(
                            checked = onlyAvailable,
                            onCheckedChange = { filterOptions.updateOnlyAvailable(!onlyAvailable) }
                        )
                    }
                    Spacer(Modifier.height(12.dp))

                    // ---------------------------- type ----------------------------
                    Text(
                        stringResource(R.string.charger_type),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ChargerType.entries.forEach { type ->
                            TypeChargerButton(
                                type = type,
                                isSelected = selectedTypes.contains(type),
                                onClick = {
                                    if (selectedTypes.contains(type)) {
                                        filterOptions.updateSelectedTypes(selectedTypes - type)
                                    } else {
                                        filterOptions.updateSelectedTypes(selectedTypes + type)
                                    }
                                }
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))

                    // ---------------------------- Power  ----------------------------
                    Text(
                        stringResource(R.string.charger_power),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ChargerPower.entries.forEach { power ->
                            PowerTagButton(
                                power = power,
                                selected = selectedPowers.contains(power),
                                onClick = {
                                    if (selectedPowers.contains(power)) {
                                        filterOptions.updateSelectedPowers(selectedPowers - power)
                                    } else {
                                        filterOptions.updateSelectedPowers(selectedPowers + power)
                                    }
                                }
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))

                    // ---------------------------- price ----------------------------
                    Text(
                        stringResource(R.string.price_range),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    RangeSlider(
                        range = priceRange,
                        valueRange = 0f..20f,
                        onValueChange = { filterOptions.updatePriceRange(it) },
                        valueFormatter = { "€${"%.2f".format(it.start)} - €${"%.2f".format(it.endInclusive)}" },
                        steps = 39
                    )
                    Spacer(Modifier.height(12.dp))

                    // ---------------------------- payment methods ----------------------------
                    Text(
                        stringResource(R.string.payment_methods),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    PaymentMethodsSelection(
                        selectedMethods = selectedPayments,
                        onSelectionChange = {
                            filterOptions.updateSelectedPayments(it)
                        }
                    )
                    Spacer(Modifier.height(12.dp))

                    // ---------------------------- nearby services ----------------------------
                    Text(
                        stringResource(R.string.nearby_services),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        maxItemsInEachRow = Int.MAX_VALUE
                    ) {
                        NearbyService.entries.forEach { service ->
                            val selected = selectedServices.contains(service)
                            NearbyServicesButton(
                                service = service,
                                isSelected = selected,
                                onClick = {
                                    if (selectedServices.contains(service)) {
                                        filterOptions.updateSelectedServices(selectedServices - service)
                                    } else {
                                        filterOptions.updateSelectedServices(selectedServices + service)
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f, fill = false)
                                    .padding(horizontal = 2.dp)
                                    .widthIn(min = 80.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))

                    // ---------------------------- distance ----------------------------
                    Text(
                        stringResource(R.string.maximum_distance),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Slider(
                        value = maxDistance,
                        onValueChange = { filterOptions.updateMaxDistance(it) },
                        valueRange = 1f..100f,
                        steps = 19
                    )
                    Text(
                        "${maxDistance.toInt()} km",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))

                    // ---------------------------- travel time ----------------------------
                    Text(
                        stringResource(R.string.maximum_travel_time),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Slider(
                        value = maxTravelTime,
                        onValueChange = { filterOptions.updateMaxTravelTime(it) },
                        valueRange = 5f..120f,
                        steps = 23
                    )
                    Text(
                        "${maxTravelTime.toInt()} min",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
