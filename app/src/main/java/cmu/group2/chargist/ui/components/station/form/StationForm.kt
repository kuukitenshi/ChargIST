package cmu.group2.chargist.ui.components.station.form

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cmu.group2.chargist.R
import cmu.group2.chargist.data.api.GeoResponse
import cmu.group2.chargist.data.model.ChargerBundle
import cmu.group2.chargist.data.model.EmptyStationFormData
import cmu.group2.chargist.data.model.GeoLocation
import cmu.group2.chargist.data.model.NearbyService
import cmu.group2.chargist.data.model.StationFormData
import cmu.group2.chargist.readUriBytes
import cmu.group2.chargist.ui.components.chargers.ChargerBundleCard
import cmu.group2.chargist.ui.components.common.ClickableImage
import cmu.group2.chargist.ui.components.common.ImageSelectorDialog
import cmu.group2.chargist.ui.components.common.PageIndicatorHorizontalScroll
import cmu.group2.chargist.ui.components.common.custom.CustomTextField
import cmu.group2.chargist.ui.components.common.stepper.MultiStepper
import cmu.group2.chargist.ui.components.common.stepper.Step
import cmu.group2.chargist.ui.components.station.NearbyServiceCard
import cmu.group2.chargist.ui.components.station.NearbyServicesRow
import cmu.group2.chargist.ui.components.station.PaymentMethodsRow
import cmu.group2.chargist.ui.components.station.PaymentMethodsSelection
import coil.compose.AsyncImage

@Composable
fun StationForm(
    initialData: StationFormData? = null,
    onComplete: (StationFormData) -> Unit,
    isEditMode: Boolean = false,
    onLocationChanged: (GeoLocation) -> Unit,
    onZoomChanged: (Double) -> Unit,
    currentLocation: GeoLocation?,
    userCurrentLocation: GeoLocation? = null,
    currentZoom: Double? = null,

    // search bar
    searchQuery: String,
    searchResults: List<GeoResponse>,
    onSearchChange: (String) -> Unit,
    onClearSearch: () -> Unit,
) {
    var formData by remember { mutableStateOf(initialData ?: EmptyStationFormData) }

    MultiStepper(
        canProceed = {
            when (it) {
                1 -> formData.name.isNotBlank() && formData.paymentMethods.isNotEmpty() && formData.location != null
                2 -> formData.bundles.isNotEmpty()
                else -> true
            }
        },
        onComplete = { onComplete(formData) },
        steps = listOf(
            Step(title = stringResource(R.string.station_info)) {
                StepOne(
                    formData = formData,
                    onUpdateForm = { formData = it },
                    onLocationChanged = onLocationChanged,
                    currentLocation = if (isEditMode) formData.location
                        ?: currentLocation else userCurrentLocation,
                    onZoomChanged = onZoomChanged,
                    currentZoom = currentZoom ?: 15.0,
                    searchQuery = searchQuery,
                    searchResults = searchResults,
                    onSearchChange = onSearchChange,
                    onClearSearch = onClearSearch,
                )
            },
            Step(title = stringResource(R.string.charger_details)) {
                StepTwo(
                    formData = formData,
                    onUpdateForm = { formData = it },
                    isEditMode = isEditMode
                )
            },
            Step(title = "Nearby services") {
                StepThree(formData = formData, onUpdateForm = { formData = it })
            },
            Step(title = stringResource(R.string.confirm_station_details)) {
                StepFour(formData = formData)
            }
        )
    )
}

@SuppressLint("DefaultLocale")
@Composable
private fun StepOne(
    formData: StationFormData,
    onUpdateForm: (StationFormData) -> Unit,
    onLocationChanged: (GeoLocation) -> Unit,
    currentLocation: GeoLocation?,
    currentZoom: Double?,
    onZoomChanged: (Double) -> Unit,

    // search bar
    searchQuery: String,
    searchResults: List<GeoResponse>,
    onSearchChange: (String) -> Unit,
    onClearSearch: () -> Unit,
) {
    val context = LocalContext.current

    var showEditImage by remember { mutableStateOf(false) }

    if (showEditImage) {
        ImageSelectorDialog(
            title = stringResource(R.string.selected_photo),
            onDismiss = { showEditImage = false },
            onSelect = { uri ->
                showEditImage = false
                val bytes = context.readUriBytes(uri)
                onUpdateForm(formData.copy(imageUri = uri, imageBytes = bytes))
            }
        )
    }

    Spacer(modifier = Modifier.height(12.dp))
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        // --- image ---------------------
        ClickableImage(
            imageUri = formData.imageUri,
            fallbackImage = painterResource(R.drawable.default_station_image),
            onClick = { showEditImage = true }
        )
        Spacer(modifier = Modifier.height(16.dp))

        // --- name ---------------------
        CustomTextField(
            value = formData.name,
            onValueChange = { onUpdateForm(formData.copy(name = it)) },
            label = stringResource(R.string.station_name)
        )
        Spacer(modifier = Modifier.height(40.dp))

        // --- location ----------------------
        LocationInput(
            currentLocation = currentLocation,
            currentZoom = currentZoom,
            onLocationSelected = { loc ->
                val geo = GeoLocation(loc.latitude, loc.longitude)
                onUpdateForm(formData.copy(location = geo))
                onLocationChanged(geo)
            },
            onLocationDragged = { loc ->
                val geo = GeoLocation(loc.latitude, loc.longitude)
                onUpdateForm(formData.copy(location = geo))
                onLocationChanged(geo)
            },
            onZoomChanged = { zoom -> onZoomChanged(zoom) },
            onLocationConfirmed = { loc ->
                val geo = GeoLocation(loc.latitude, loc.longitude)
                onUpdateForm(formData.copy(location = geo))
                onLocationChanged(geo)
            },
            searchQuery = searchQuery,
            searchResults = searchResults,
            onSearchChange = onSearchChange,
            onClearSearch = onClearSearch,
            onNavigateToLocation = { lat, lon ->
                Log.d("SEARCH", "stepone navigate lat lon: $lat $lon")
                val geo = GeoLocation(lat, lon)
                onUpdateForm(formData.copy(location = geo))
                Log.d("SEARCH", "stepone navigate: ${formData.location}")
            },
        )
        if (formData.location != null) {
            Text(
                text = stringResource(R.string.selected_loc),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 8.dp)
            )
            val lat = formData.location!!.latitude
            val lon = formData.location!!.longitude
            Log.d("SEARCH", "stepone print: ${formData.location}")
            Text(text = "${String.format("%.5f", lat)}; ${String.format("%.5f", lon)}")
        }
        Spacer(modifier = Modifier.height(45.dp))

        // ------------------ payment ------------------
        Text(
            text = stringResource(R.string.select_pay_meth),
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 8.dp)
        )
        Box(
            modifier = Modifier.padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            PaymentMethodsSelection(
                selectedMethods = formData.paymentMethods,
                onSelectionChange = { methods -> onUpdateForm(formData.copy(paymentMethods = methods)) }
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun StepTwo(
    formData: StationFormData,
    onUpdateForm: (StationFormData) -> Unit,
    isEditMode: Boolean
) {
    var showDialog by remember { mutableStateOf(false) }
    var isDeleteMode by remember { mutableStateOf(false) }
    var selectedBundles by remember { mutableStateOf(emptyList<ChargerBundle>()) }
    var bundleToEdit by remember { mutableStateOf<ChargerBundle?>(null) }
    val context = LocalContext.current

    if (showDialog) {
        Log.d("BUNDLES", "station form type ${formData.bundles}")
        AddChargerPopUp(
            existingBundles = formData.bundles,
            onDismiss = { showDialog = false },
            title = if (isEditMode) stringResource(R.string.edit_charger_details) else stringResource(
                R.string.add_charger_details
            ),
            bundleToEdit = bundleToEdit,
            onAddCharger = { newBundle ->
                val isDuplicate = formData.bundles.any {
                    it.type == newBundle.type && it.power == newBundle.power && it != bundleToEdit
                }
                if (isDuplicate) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.duplicate_charger_bundle),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val updatedBundles = if (bundleToEdit != null) {
                        formData.bundles.map { if (it == bundleToEdit) newBundle else it }
                    } else {
                        formData.bundles + newBundle
                    }
                    val newFormData = formData.copy(bundles = updatedBundles)
                    Log.d("BUNDLES", "new form station form type ${newFormData.bundles}")
                    onUpdateForm(newFormData)
                    showDialog = false
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = {
                    showDialog = true
                    bundleToEdit = null
                },
                modifier = Modifier.weight(.9f)
            ) {
                Text(stringResource(R.string.add_charger))
            }
            Spacer(modifier = Modifier.width(8.dp))

            TrashToggleButton(
                isActive = isDeleteMode,
                onToggle = {
                    selectedBundles = emptyList()
                    isDeleteMode = !isDeleteMode
                }
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        formData.bundles.forEach { bundle ->
            ChargerBundleCard(
                chargerBundle = bundle,
                onClick = {
                    if (isDeleteMode) {
                        selectedBundles = if (selectedBundles.contains(bundle)) {
                            selectedBundles - bundle
                        } else {
                            selectedBundles + bundle
                        }
                    } else {
                        bundleToEdit = bundle
                        showDialog = true
                    }
                },
                showCheckCircle = isDeleteMode,
                isChecked = selectedBundles.contains(bundle)
            )
            Spacer(modifier = Modifier.height(6.dp))
        }

        if (isDeleteMode && selectedBundles.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = {
                    onUpdateForm(formData.copy(bundles = formData.bundles - selectedBundles.toSet()))
                    selectedBundles = emptyList()
                    isDeleteMode = false
                }) {
                    Text(stringResource(R.string.delete))
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun StepThree(formData: StationFormData, onUpdateForm: (StationFormData) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        NearbyService.entries.toList().forEach { nearbyService ->
            NearbyServiceCard(
                nearbyService = nearbyService,
                showCheckCircle = true,
                isChecked = formData.nearbyServices.contains(nearbyService),
                onClick = {
                    if (formData.nearbyServices.contains(nearbyService)) {
                        onUpdateForm(formData.copy(nearbyServices = formData.nearbyServices - nearbyService))
                    } else {
                        onUpdateForm(formData.copy(nearbyServices = formData.nearbyServices + nearbyService))
                    }
                }
            )
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
private fun StepFour(formData: StationFormData) {
    val pagerState = rememberPagerState(pageCount = { formData.bundles.size })
    val fallbackImage = painterResource(R.drawable.default_station_image)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                //------------ photo ------------
                AsyncImage(
                    model = formData.imageUri,
                    contentDescription = stringResource(R.string.station_img_desc),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    error = fallbackImage,
                    fallback = fallbackImage
                )
                Spacer(modifier = Modifier.height(16.dp))

                // --------------- Name----------
                Text(
                    text = formData.name,
                    fontSize = 20.sp,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                // ---------- Location -----------------
                Text(
                    text = buildAnnotatedString {
                        pushStyle(
                            SpanStyle(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 16.sp
                            )
                        )
                        append(stringResource(R.string.location) + ": ")
                        pop()
                        val lat = formData.location!!.latitude
                        val lon = formData.location!!.longitude
                        append("${String.format("%.5f", lat)}; ${String.format("%.5f", lon)}")
                        pushStyle(
                            SpanStyle(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 16.sp
                            )
                        )
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))

                //-------------  Chargers Header -------------
                Text(
                    text = stringResource(R.string.chargers) + " (${formData.bundles.size} " + stringResource(
                        R.string.types
                    ) + ")",
                    fontSize = 20.sp,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                // -------------- scroll charger cards --------------------
                HorizontalPager(state = pagerState) { page ->
                    ChargerBundleCard(
                        chargerBundle = formData.bundles[page],
                        modifier = Modifier,
                        onClick = { }
                    )
                }
                // ---------- page indicator balls -------------------
                Spacer(modifier = Modifier.height(8.dp))
                PageIndicatorHorizontalScroll(
                    pagerState = pagerState,
                    totalItems = formData.bundles.size
                )
            }
        }

        //-------------  Nearby services -------------
        Text(
            text = stringResource(R.string.nearby) + " (%s)".format(formData.nearbyServices.size),
            fontSize = 20.sp,
            style = MaterialTheme.typography.headlineMedium.copy(
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        )
        // -------------- nearby list --------------------
        Row(
            modifier = Modifier.fillMaxWidth(0.8f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NearbyServicesRow(formData.nearbyServices, alignment = Alignment.CenterHorizontally)
        }
        Spacer(modifier = Modifier.height(16.dp))

        // ------------- payment -------------------
        Text(
            text = stringResource(R.string.payment_accepted),
            fontSize = 20.sp,
            style = MaterialTheme.typography.headlineMedium.copy(
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        )
        // ----------- payment list ---------------
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            PaymentMethodsRow(formData.paymentMethods, alignment = Alignment.CenterHorizontally)
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}



