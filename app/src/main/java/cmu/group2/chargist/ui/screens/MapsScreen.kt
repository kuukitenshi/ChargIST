package cmu.group2.chargist.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.location.Location
import android.util.Log
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.scale
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import cmu.group2.chargist.R
import cmu.group2.chargist.Screens
import cmu.group2.chargist.calculateDistanceMeters
import cmu.group2.chargist.data.model.GeoLocation
import cmu.group2.chargist.data.model.Station
import cmu.group2.chargist.ui.components.common.custom.CustomCircleButton
import cmu.group2.chargist.ui.components.map.SwipeUpMap
import cmu.group2.chargist.ui.components.map.filter.FilterDialog
import cmu.group2.chargist.viewmodel.MapsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.infowindow.InfoWindow
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

private val INITIAL_POSITION: GeoPoint = GeoPoint(38.736691, -9.138769)
private const val INITIAL_ZOOM = 18.0
private var permissionRequested = false

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapsScreen(
    navController: NavController,
    locationPermissions: ActivityResultLauncher<Array<String>>,
    navLocation: GeoLocation? = null,
    viewModel: MapsViewModel = viewModel()
) {
    val context = LocalContext.current

    val lastDragLocation by viewModel.lastDragLocation.collectAsState()
    val lastZoom by viewModel.lastZoom.collectAsState()
    val stations by viewModel.allStations.collectAsState()
    val currentLocation by viewModel.currentLocation.collectAsState()
    val shouldCenterMap by viewModel.shouldCenterMap.collectAsState()
    val filterOptions by viewModel.filterOptions.collectAsState()
    val filterFunc by viewModel.filterFunc.collectAsState()
    val selectedSortOption by viewModel.selectedSortOption.collectAsState()
    val sortComparator by viewModel.sortComparator.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()

    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()

    val mapView = remember {
        MapView(context)
    }

    var isSheetOpen by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showFilters by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    //------------------------ perms ----------------------------
    SideEffect {
        if (!permissionRequested) {
            Log.d("LocPerm", "Requesting permissions")
            permissionRequested = true
            locationPermissions.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    LaunchedEffect(stations) {
        mapView.overlays.filterIsInstance<Marker>().forEach { it.closeInfoWindow() }
        mapView.overlays.removeAll { overlay -> overlay is Marker }
        stations.forEach { (station, favorite) ->
            val geoPoint = GeoPoint(station.location.latitude, station.location.longitude)
            val marker = Marker(mapView)
            marker.position = geoPoint
            marker.icon =
                if (favorite) getMarkerIcon(context, R.drawable.marker_fav)
                else getMarkerIcon(context, R.drawable.marker_normal)
            val infoWindow = RatingInfoWindow(station, mapView)
            marker.infoWindow = infoWindow
            marker.setOnMarkerClickListener { _, _ ->
                navController.navigate(Screens.StationDetails.route + "/${station.id}")
                true
            }
            mapView.overlayManager.add(marker)
        }
    }

    LaunchedEffect(currentLocation) {
        mapView.overlayManager.filterIsInstance<Marker>().forEach { marker ->
            val location = GeoLocation(marker.position.latitude, marker.position.longitude)
            if (isWithinRadius(currentLocation, location)) {
                marker.showInfoWindow()
            } else {
                marker.closeInfoWindow()
            }
        }
        if (currentLocation != null && !mapView.overlays.any { it is MyLocationNewOverlay }) {
            Log.d("Map", "Add location overlay")
            val locationProvider = GpsMyLocationProvider(context)
            val overlay = MyLocationNewOverlay(locationProvider, mapView)
            overlay.enableMyLocation()
            overlay.setDirectionIcon(getDirectionIcon(context))
            overlay.setDirectionAnchor(0.5f, 0.5f)
            overlay.disableFollowLocation()
            mapView.overlays.add(overlay)
            mapView.invalidate()
        }
    }

    DisposableEffect(shouldCenterMap) {
        currentLocation?.let {
            if (shouldCenterMap) {
                val geoPoint = GeoPoint(it.latitude, it.longitude)
                mapView.controller.setCenter(geoPoint)
                viewModel.onCenter()
            }
        }
        onDispose {
        }
    }

    DisposableEffect(mapView) {
        val mapListener = object : MapListener {
            override fun onScroll(event: ScrollEvent): Boolean {
                val center = mapView.mapCenter
                val geoLoc = GeoLocation(center.latitude, center.longitude)
                viewModel.onDrag(geoLoc)
                return true
            }

            override fun onZoom(event: ZoomEvent): Boolean {
                viewModel.onZoom(event.zoomLevel)
                return true
            }
        }
        mapView.addMapListener(mapListener)
        mapView.invalidate()
        onDispose {
            mapView.removeMapListener(mapListener)
        }
    }

    //------------------------ filter ------------------
    if (showFilters) {
        FilterDialog(
            onDismiss = { showFilters = false },
            filterOptions = filterOptions,
            onApply = {
                viewModel.applyFilter()
                showFilters = false
            },
            onClearFilters = { viewModel.clearFilterOptions() }
        )
    }

    Scaffold { innerPadding ->
        AndroidView(
            factory = {
                Log.d("OSMFactory", "Creating map")
                val center = navLocation?.let { GeoPoint(it.latitude, it.longitude) }
                    ?: lastDragLocation?.let { GeoPoint(it.latitude, it.longitude) }
                    ?: INITIAL_POSITION
                val zoom = navLocation?.let { INITIAL_ZOOM } ?: lastZoom ?: INITIAL_ZOOM
                mapView.apply {
                    setMultiTouchControls(true)
                    zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
                    setTileSource(TileSourceFactory.MAPNIK)
                    controller.setCenter(center)
                    controller.setZoom(zoom)
                }
                mapView.invalidate()
                mapView
            }
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // ---------------- buttons ---------------------
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 96.dp, end = 18.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    CustomCircleButton(
                        icon = Icons.Default.MyLocation,
                        description = stringResource(R.string.center_loc_desc),
                        onClick = {
                            currentLocation?.let {
                                val geoPoint = GeoPoint(it.latitude, it.longitude)
                                mapView.controller.animateTo(geoPoint)
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    CustomCircleButton(
                        icon = Icons.Default.Add,
                        description = stringResource(R.string.add_station_desc),
                        onClick = { navController.navigate(Screens.AddStation.route) }
                    )
                }
            }
            // -------- search bar with profile --------
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 24.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .clip(MaterialTheme.shapes.large)
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable {
                            coroutineScope.launch {
                                isSheetOpen = true
                                sheetState.show()
                            }
                        }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = stringResource(R.string.search_desc),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = searchQuery.ifEmpty { stringResource(R.string.search_address) },
                        color = Color.Gray,
                        modifier = Modifier.weight(1f)
                    )
                    // --------------- profile -------------------
                    Spacer(modifier = Modifier.width(16.dp))
                    Box(
                        modifier = Modifier
                            .padding(start = 0.dp, top = 0.dp, bottom = 0.dp, end = 8.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                            .clickable { navController.navigate(Screens.Profile.route) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = stringResource(R.string.profile_icon_desc),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
            //------------------------ swipe up -----------------------
            SwipeUpMap(
                isVisible = isSheetOpen,
                sheetState = sheetState,
                coroutineScope = coroutineScope,
                onDismiss = { isSheetOpen = false },
                searchQuery = searchQuery,
                searchResults = searchResults,
                onSearchChange = { newQuery ->
                    searchQuery = newQuery
                    viewModel.onSearchChange(newQuery)
                },
                userLocation = currentLocation,
                onClearSearch = {
                    searchQuery = ""
                    viewModel.onClearSearch()
                },
                stations = stations.map { (s, _) -> s }.filter(filterFunc)
                    .sortedWith(sortComparator),
                selectedSortOption = selectedSortOption,
                onStationClick = {
                    isSheetOpen = false
                    navController.navigate(Screens.StationDetails.route + "/${it.id}")
                },
                onFilterClick = { showFilters = true },
                onNavigateToLocation = { lat, lon ->
                    isSheetOpen = false
                    mapView.controller.animateTo(GeoPoint(lat, lon))
                },
                onApplySortOption = { selected -> viewModel.applySortOptions(selected) },
                listState = listState,
                onMoreClick = {
                    viewModel.onLoadMore()
                }
            )
        }
    }
}

fun getMarkerIcon(context: Context, rid: Int): Drawable {
    val original = BitmapFactory.decodeResource(context.resources, rid)
    val width = original.width
    val height = original.height
    val scalingFactor = 45
    val scaled = original.scale(width / scalingFactor, height / scalingFactor, false)
        .toDrawable(context.resources)
    return scaled
}

private fun getDirectionIcon(context: Context): Bitmap {
    val original = BitmapFactory.decodeResource(context.resources, R.drawable.navvv)
    val width = original.width
    val height = original.height
    val scalingFactor = 20
    val scaled = original.scale(width / scalingFactor, height / scalingFactor)
    return scaled
}

private fun isWithinRadius(
    current: Location?,
    stationLocation: GeoLocation,
    radiusKm: Float = 5f
): Boolean {
    return current?.let {
        val distanceMeters = calculateDistanceMeters(
            GeoLocation(it.latitude, it.longitude),
            stationLocation
        ) / 1000
        distanceMeters <= radiusKm
    } == true
}

private class RatingInfoWindow(val station: Station, mapView: MapView) :
    InfoWindow(R.layout.marker_rating_layout, mapView) {
    @SuppressLint("SetTextI18n")
    override fun onOpen(item: Any?) {
        val ratingText = mView.findViewById<TextView>(R.id.ratingText)
        ratingText.text = "%.1f".format(station.avgRating)
    }

    override fun onClose() {
    }
}
