package cmu.group2.chargist.ui.components.station.form

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import cmu.group2.chargist.R
import cmu.group2.chargist.data.api.GeoResponse
import cmu.group2.chargist.data.model.GeoLocation
import cmu.group2.chargist.ui.components.common.OrDivider
import cmu.group2.chargist.ui.components.map.SearchBarWithClear
import cmu.group2.chargist.ui.screens.getMarkerIcon
import com.google.android.gms.location.LocationServices
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@Composable
fun LocationInput(
    onLocationSelected: (Location) -> Unit,
    onLocationDragged: (GeoLocation) -> Unit,
    onZoomChanged: (Double) -> Unit,
    onLocationConfirmed: (GeoLocation) -> Unit,
    currentLocation: GeoLocation?,
    currentZoom: Double?,

    // search bar
    searchQuery: String,
    searchResults: List<GeoResponse>,
    onSearchChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    onNavigateToLocation: (Double, Double) -> Unit,
) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        LocationPickerPopUp(
            onDismiss = { showDialog = false },
            onCurrentInput = {
                showDialog = false
                getCurrentLocation(context, onLocationSelected)
            },
            currentLocation = currentLocation,
            currentZoom = currentZoom,
            onLocationDragged = onLocationDragged,
            onZoomChanged = onZoomChanged,
            onLocationConfirmed = {
                showDialog = false
                onLocationConfirmed(it)
            },
            searchQuery = searchQuery,
            searchResults = searchResults,
            onSearchChange = onSearchChange,
            onClearSearch = onClearSearch,
            onNavigateToLocation = onNavigateToLocation
        )
    }

    Button(
        onClick = { showDialog = true },
        modifier = Modifier.fillMaxWidth(0.8f)
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(stringResource(R.string.select_loc))
    }
    Spacer(modifier = Modifier.height(16.dp))
}


@SuppressLint("ClickableViewAccessibility")
@Composable
private fun LocationPickerPopUp(
    onDismiss: () -> Unit,
    onCurrentInput: () -> Unit,
    currentLocation: GeoLocation?,
    currentZoom: Double?,
    onLocationDragged: (GeoLocation) -> Unit,
    onZoomChanged: (Double) -> Unit,
    onLocationConfirmed: (GeoLocation) -> Unit,

    // search bar
    searchQuery: String,
    searchResults: List<GeoResponse>,
    onSearchChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    onNavigateToLocation: (Double, Double) -> Unit,
) {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }
    var selectedLocation by remember { mutableStateOf(currentLocation) }
    val focusRequester = remember { FocusRequester() }

    DisposableEffect(mapView) {
        val normalMarker = getMarkerIcon(context, R.drawable.marker_normal)
        val marker = Marker(mapView).apply {
            icon = normalMarker
            isDraggable = false
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        }

        fun updateMarkerPosition(location: GeoLocation?) {
            location?.let {
                marker.position = GeoPoint(it.latitude, it.longitude)
                if (!mapView.overlays.contains(marker)) {
                    mapView.overlays.add(marker)
                }
                mapView.invalidate()
            }
        }

        val listener = object : MapListener {
            override fun onScroll(event: ScrollEvent): Boolean {
                val center = mapView.mapCenter
                selectedLocation = GeoLocation(center.latitude, center.longitude)
                onLocationDragged(selectedLocation!!)
                return true
            }

            override fun onZoom(event: ZoomEvent): Boolean {
                onZoomChanged(event.zoomLevel)
                return true
            }
        }
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapView.zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)

        if (currentLocation != null) {
            val startPoint = GeoPoint(currentLocation.latitude, currentLocation.longitude)
            mapView.controller.setZoom(currentZoom ?: 18.0)
            mapView.controller.setCenter(startPoint)
            selectedLocation = currentLocation
            updateMarkerPosition(currentLocation)
        } else {
            mapView.controller.setZoom(currentZoom ?: 18.0)
        }
        mapView.addMapListener(listener)

        mapView.setOnTouchListener { _, event ->
            if (event.action == android.view.MotionEvent.ACTION_UP) {
                val projection = mapView.projection
                val geoPoint = projection.fromPixels(event.x.toInt(), event.y.toInt()) as GeoPoint
                selectedLocation = GeoLocation(geoPoint.latitude, geoPoint.longitude)
                onLocationDragged(selectedLocation!!)
                updateMarkerPosition(selectedLocation)
                true
            } else {
                false
            }
        }
        mapView.invalidate()

        onDispose {
            mapView.removeMapListener(listener)
            mapView.setOnTouchListener(null)
        }
    }

    AlertDialog(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .shadow(8.dp, RoundedCornerShape(20.dp)),
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.choose_loc_meth),
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ----------- map picker ----------
                AndroidView(
                    factory = { mapView },
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(250.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
                //--------------- or -----------------
                Spacer(modifier = Modifier.height(16.dp))
                OrDivider()
                Spacer(modifier = Modifier.height(16.dp))
                // ----------- curr loc ----------
                Button(
                    onClick = onCurrentInput,
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(text = stringResource(R.string.use_curr_loc))
                }
                //--------------- or -----------------
                Spacer(modifier = Modifier.height(16.dp))
                OrDivider()
                Spacer(modifier = Modifier.height(16.dp))
                // ----------- curr loc ----------
                SearchBarWithClear(
                    searchQuery = searchQuery,
                    searchResults = searchResults,
                    stationsList = emptyList(),
                    onSearchChange = onSearchChange,
                    onClearSearch = onClearSearch,
                    focusRequester = focusRequester,
                    autoFocus = true,
                    onNavigateToLocation = { lat, lon ->
                        selectedLocation = GeoLocation(lat, lon)
                        onNavigateToLocation(lat, lon)
                    },
                    isInMapScreen = false
                )
            }
        },
        // --------- confirm /cancel btn -------------
        confirmButton = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.cancel))
                }
                Button(onClick = { selectedLocation?.let { onLocationConfirmed(it) } }) {
                    Text(stringResource(R.string.done))
                }
            }
        },
    )
}

@SuppressLint("MissingPermission")
private fun getCurrentLocation(context: Context, callback: (Location) -> Unit) {
    val locationClient = LocationServices.getFusedLocationProviderClient(context)
    locationClient.lastLocation.addOnSuccessListener { callback(it) }
}
