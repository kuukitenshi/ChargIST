package cmu.group2.chargist.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import cmu.group2.chargist.R
import cmu.group2.chargist.ui.components.common.BackArrow
import cmu.group2.chargist.ui.components.common.custom.CustomMainTitle
import cmu.group2.chargist.ui.components.station.form.StationForm
import cmu.group2.chargist.viewmodel.AddStationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStationScreen(
    navController: NavController, viewModel: AddStationViewModel = viewModel()
) {
    val context = LocalContext.current
    val currentLocation by viewModel.lastDragLocation.collectAsState()
    val currentZoom by viewModel.lastZoom.collectAsState()
    val userCurrentLocation by viewModel.userCurrentLocation.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()

    val errorImg = stringResource(R.string.error_add_station_img)
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.locationGranted()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { CustomMainTitle(title = stringResource(R.string.add_station_desc)) },
                navigationIcon = { BackArrow(onBackClick = { navController.popBackStack() }) }
            )
        },
    ) { innerPadding ->
        val customTopPadding = 70.dp
        val adjustedPadding = PaddingValues(
            start = innerPadding.calculateStartPadding(LayoutDirection.Ltr),
            top = customTopPadding,
            end = innerPadding.calculateEndPadding(LayoutDirection.Ltr),
            bottom = innerPadding.calculateBottomPadding()
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(adjustedPadding)
        ) {
            val created = stringResource(R.string.station_created)
            val errorCreate = stringResource(R.string.failed_create_station)
            StationForm(
                onComplete = { formData ->
                    try {
                        viewModel.submitStation(
                            formData,
                            onSuccess = {
                                Toast.makeText(context, created, Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            },
                            onFail = {
                                Toast.makeText(context, errorCreate, Toast.LENGTH_SHORT).show()
                            })
                    } catch (_: Exception) {
                        Toast.makeText(context, errorImg, Toast.LENGTH_SHORT).show()
                    }
                },
                onLocationChanged = { newLocation -> viewModel.onDrag(newLocation) },
                onZoomChanged = { newZoom -> viewModel.onZoom(newZoom) },
                currentLocation = currentLocation,
                currentZoom = currentZoom,
                userCurrentLocation = userCurrentLocation,
                //----------- search bar ------------------------------------------
                searchQuery = searchQuery,
                searchResults = searchResults,
                onSearchChange = { newQuery ->
                    searchQuery = newQuery
                    viewModel.onSearchChange(newQuery)
                },
                onClearSearch = {
                    searchQuery = ""
                    viewModel.onClearSearch()
                }
            )
        }
    }
}
