package cmu.group2.chargist.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import cmu.group2.chargist.R
import cmu.group2.chargist.Screens
import cmu.group2.chargist.popAll
import cmu.group2.chargist.ui.components.common.BackArrow
import cmu.group2.chargist.ui.components.common.EmptyStateMessage
import cmu.group2.chargist.ui.components.common.custom.CustomMainTitle
import cmu.group2.chargist.ui.components.profile.EditProfileDialog
import cmu.group2.chargist.ui.components.profile.LogoutPopUp
import cmu.group2.chargist.ui.components.profile.ProfileInfo
import cmu.group2.chargist.ui.components.station.ScrollableStationList
import cmu.group2.chargist.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = viewModel()
) {
    val context = LocalContext.current
    val favStations by viewModel.favStations.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    var showEditDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    val errorName = stringResource(R.string.error_change_name)
    val errorPic = stringResource(R.string.error_change_picture)
    val errorChangeName = stringResource(R.string.failed_change_name)

    if (showLogoutDialog) {
        LogoutPopUp(
            onConfirm = {
                showLogoutDialog = false
                viewModel.logout()
                navController.navigate(Screens.Opening.route) {
                    navController.popAll()
                }
            },
            onDismiss = { showLogoutDialog = false }
        )
    }

    if (showEditDialog) {
        EditProfileDialog(
            startName = currentUser?.name ?: "",
            onDismiss = { showEditDialog = false },
            onSave = { newName ->
                try {
                    viewModel.updateName(newName, onFail = {
                        Toast.makeText(context, errorChangeName, Toast.LENGTH_SHORT).show();
                    })
                } catch (_: Exception) {
                    Toast.makeText(context, errorName, Toast.LENGTH_SHORT).show()
                }
                showEditDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            // ---------- title + arrow -----------------
            CenterAlignedTopAppBar(
                title = {
                    CustomMainTitle(
                        title = stringResource(R.string.profile_icon_desc),
                        modifier = Modifier
                    )
                },
                navigationIcon = { BackArrow(onBackClick = { navController.popBackStack() }) }
            )
        },
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(top = 0.dp)
                    .padding(start = 18.dp, end = 18.dp)
            ) {
                // ------------- profile details ----------------------------
                ProfileInfo(
                    name = currentUser?.name ?: "",
                    username = currentUser?.username ?: "",
                    picture = currentUser?.pictureUrl,
                    onEditProfileClick = { showEditDialog = true },
                    onImageSelect = { uri ->
                        try {
                            viewModel.updateProfileImage(context, uri, onFail = {
                                Toast.makeText(context, errorPic, Toast.LENGTH_SHORT).show();
                            })
                        } catch (_: Exception) {
                            Toast.makeText(context, errorPic, Toast.LENGTH_SHORT).show()
                        }
                    },
                    isGuest = currentUser?.isGuest == true,
                    onLogoutOrCreateClick = {
                        if (currentUser?.isGuest == true) {
                            navController.navigate(Screens.Register.route)
                        } else {
                            showLogoutDialog = true
                        }
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))

                // ------------ favorite list --------------------------------
                Text(
                    text = stringResource(R.string.favorite_stations_title),
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(12.dp))

                //---------- no favs ---------------------
                if (favStations.isEmpty()) {
                    EmptyStateMessage(
                        icon = Icons.Outlined.FavoriteBorder,
                        message = stringResource(R.string.no_favorites)
                    )
                } else {
                    // ------------- fav cards -----------------
                    ScrollableStationList(
                        stations = favStations,
                        onStationClick = { station ->
                            navController.navigate(Screens.StationDetails.route + "/${station.id}")
                        },
                        onNavigateToLocation = { stationLat, stationLon ->
                            navController.navigate(Screens.Map.route + "?lat=${stationLat}&lon=${stationLon}")
                        }
                    )
                }
            }
        }
    )
}