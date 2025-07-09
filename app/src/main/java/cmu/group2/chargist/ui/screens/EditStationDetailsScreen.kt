package cmu.group2.chargist.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import cmu.group2.chargist.R
import cmu.group2.chargist.data.model.StationFormData
import cmu.group2.chargist.ui.components.common.BackArrow
import cmu.group2.chargist.ui.components.common.custom.CustomMainTitle
import cmu.group2.chargist.ui.components.station.form.StationForm
import cmu.group2.chargist.viewmodel.EditStationDetailsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditStationDetailsScreen(
    stationId: Long,
    navController: NavController,
    viewModel: EditStationDetailsViewModel = viewModel()
) {
    val context = LocalContext.current
    viewModel.fetchStation(stationId)

    val station by viewModel.station.collectAsState()
    val currentLocation by viewModel.lastDragLocation.collectAsState()
    val currentZoom by viewModel.lastZoom.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()

    val errorImg = stringResource(R.string.error_edit_station_img)
    val errorEdit = stringResource(R.string.failed_edit_station)
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { CustomMainTitle(title = stringResource(R.string.edit_station)) },
                navigationIcon = { BackArrow(onBackClick = { navController.popBackStack() }) }
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            station?.let {
                StationForm(
                    initialData = StationFormData.fromStation(it),
                    onComplete = { formData ->
                        try {
                            viewModel.updateStation(
                                it,
                                formData,
                                onComplete = { navController.popBackStack() },
                                onFail = {
                                    Toast.makeText(context, errorEdit, Toast.LENGTH_SHORT).show()
                                })
                        } catch (_: Exception) {
                            Toast.makeText(context, errorImg, Toast.LENGTH_SHORT).show()
                        }
                    },
                    isEditMode = true,
                    onLocationChanged = { newLocation -> viewModel.onDrag(newLocation) },
                    onZoomChanged = { newZoom -> viewModel.onZoom(newZoom) },
                    currentLocation = currentLocation,
                    currentZoom = currentZoom,
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
            } ?: CircularProgressIndicator()
        }
    }
}




