package cmu.group2.chargist.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.BatteryAlert
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import cmu.group2.chargist.R
import cmu.group2.chargist.Screens
import cmu.group2.chargist.ui.components.common.BackArrow
import cmu.group2.chargist.ui.components.common.EmptyStateMessage
import cmu.group2.chargist.ui.components.common.custom.CustomMainTitle
import cmu.group2.chargist.ui.components.reviews.ReviewsSection
import cmu.group2.chargist.ui.components.station.ChargerBundlesScroller
import cmu.group2.chargist.ui.components.station.CoordsButton
import cmu.group2.chargist.ui.components.station.NearbyServicesRow
import cmu.group2.chargist.ui.components.station.PaymentMethodsRow
import cmu.group2.chargist.viewmodel.StationDetailsViewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StationDetailsScreen(
    stationId: Long,
    navController: NavController,
    viewModel: StationDetailsViewModel = viewModel(),
) {
    viewModel.fetchStation(stationId)

    val station by viewModel.station.collectAsState()
    val isFavorite by viewModel.isFavorite.collectAsState()
    val ratings by viewModel.ratings.collectAsState()
    val bundles by viewModel.bundles.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val translations by viewModel.translations.collectAsState()
    val context = LocalContext.current
    val translateError by viewModel.translateError.collectAsState()
    val showDeleteDialog = remember { mutableStateOf(false) }

    val heartColor = Color(0xFFF54272)

    LaunchedEffect(translateError) {
        translateError?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            viewModel.clearTranslateError()
        }
    }

    LaunchedEffect(stationId) {
        viewModel.fetchStation(stationId)
    }

    Scaffold(
        topBar = {
            // ---------- title + arrow + edit  -----------------
            CenterAlignedTopAppBar(
                title = { CustomMainTitle(title = station?.name ?: "", modifier = Modifier) },
                navigationIcon = { BackArrow(onBackClick = { navController.popBackStack() }) },
                actions = {
                    IconButton(onClick = {
                        navController.navigate(Screens.EditStationDetails.route + "/$stationId")
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = stringResource(R.string.edit_icon)
                        )
                    }
                }
            )
        },
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(innerPadding)
                    .padding(24.dp, 8.dp)
            ) {
                station?.let { station ->
                    Spacer(Modifier.height(8.dp))
                    // ---------- image station -----------------
                    AsyncImage(
                        model = station.imageUrl,
                        contentDescription = "${station.name} " + stringResource(R.string.station_photo_desc),
                        contentScale = ContentScale.Crop,
                        fallback = painterResource(R.drawable.default_station_image),
                        error = painterResource(R.drawable.default_station_image),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(
                                2.dp,
                                MaterialTheme.colorScheme.primary,
                                RoundedCornerShape(12.dp)
                            )
                    )
                    Spacer(Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // ---------- coord  -----------------
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.coord),
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 20.sp,
                            )
                            Spacer(Modifier.height(4.dp))
                            CoordsButton(
                                coords = station.location,
                                onNavigate = { lat, lon ->
                                    navController.navigate(Screens.Map.route + "?lat=${lat}&lon=${lon}")
                                    Log.d("btnNav", "?lat=${lat}&lon=${lon}")
                                }
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val errorFavs = stringResource(R.string.failed_add_to_favorites)
                            // ---------------- heart fav --------------------
                            IconButton(
                                onClick = {
                                    viewModel.toggleFavorite(
                                        onFail = {
                                            Toast.makeText(context, errorFavs, Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                            ) {
                                Icon(
                                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                    contentDescription = stringResource(R.string.toggle_fav),
                                    tint = if (isFavorite) heartColor else Color.Gray
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // ----------- payment -------------------------
                    Text(
                        text = stringResource(R.string.payment_meth),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 20.sp,
                    )
                    Spacer(Modifier.height(4.dp))
                    PaymentMethodsRow(methods = station.paymentMethods)
                    Spacer(Modifier.height(20.dp))

                    // ---------- chargers  -----------------
                    Text(
                        text = stringResource(R.string.chargers),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 20.sp,
                    )
                    Spacer(Modifier.height(4.dp))

                    if (station.chargers.isEmpty()) {
                        EmptyStateMessage(
                            icon = Icons.Outlined.BatteryAlert,
                            message = stringResource(R.string.no_chargers_on_station)
                        )
                    } else {
                        // -------------- scroll charger cards --------------------
                        Column {
                            if (bundles.isNotEmpty()) {
                                ChargerBundlesScroller(
                                    bundles = bundles,
                                    onBundleClick = { bundle ->
                                        navController.navigate(Screens.ChargerDetails.route + "/${stationId}/type=${bundle.type}&power=${bundle.power}")
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    // ----------- nearby -------------------------
                    Text(
                        text = stringResource(R.string.nearby),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 20.sp,
                    )
                    Spacer(Modifier.height(8.dp))
                    NearbyServicesRow(services = station.nearbyServices)
                    Spacer(Modifier.height(20.dp))

                    // ---------- reviews -----------------
                    val errorSubmitRev = stringResource(R.string.failed_submit_review)
                    ReviewsSection(
                        avgRating = station.avgRating,
                        reviews = station.reviews,
                        ratingFrequency = ratings,
                        onSubmit = { rating, text ->
                            coroutineScope.launch {
                                viewModel.submitReview(
                                    text.toString(),
                                    rating,
                                    onFail = {
                                        Toast.makeText(context, errorSubmitRev, Toast.LENGTH_SHORT).show();
                                    }
                                )
                            }
                        },
                        isGuest = currentUser?.isGuest == true,
                        onTranslate = { review, comment ->
                            viewModel.translate(review, comment)
                        },
                        translations = translations,
                        onViewMore = if (station.reviews.size >= 3) {
                            { navController.navigate(Screens.Reviews.route + "/$stationId") }
                        } else null
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }
            // ---------- delete dialog -----------------
            if (showDeleteDialog.value) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog.value = false },
                    title = { Text(stringResource(R.string.delete_station)) },
                    text = { Text(stringResource(R.string.delete_station_confirmation)) },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                station?.let {
                                    coroutineScope.launch {
                                        viewModel.deleteStation(onDelete = {
                                            navController.popBackStack()
                                        })
                                    }
                                }
                                showDeleteDialog.value = false
                            }
                        ) {
                            Text(
                                stringResource(R.string.delete),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog.value = false }) {
                            Text(stringResource(R.string.cancel))
                        }
                    }
                )
            }
        })
}
