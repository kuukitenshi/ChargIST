package cmu.group2.chargist.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import cmu.group2.chargist.R
import cmu.group2.chargist.ui.components.common.BackArrow
import cmu.group2.chargist.ui.components.common.custom.CustomMainTitle
import cmu.group2.chargist.ui.components.reviews.ReviewsSection
import cmu.group2.chargist.viewmodel.ReviewViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewsScreen(
    stationId: Long,
    navController: NavController,
    viewModel: ReviewViewModel = viewModel()
) {

    viewModel.fetchStation(stationId)
    val station by viewModel.station.collectAsState()
    val context = LocalContext.current
    val translateError by viewModel.translateError.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val translations by viewModel.translations.collectAsState()
    val listState = rememberLazyListState()

    var isLoadingMore by remember { mutableStateOf(false) }

    LaunchedEffect(translateError) {
        translateError?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            viewModel.clearTranslateError()
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow {
            listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index to listState.layoutInfo.totalItemsCount
        }.collect { (lastVisibleIndex, totalCount) ->
            if (lastVisibleIndex != null && lastVisibleIndex >= totalCount - 2 && !isLoadingMore) {
                isLoadingMore = true
                viewModel.updateFetchReviews(stationId, totalCount.toLong())
                delay(500) // avoid spam
                isLoadingMore = false
            }
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow {
            listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index to listState.layoutInfo.totalItemsCount
        }.collect { (lastVisibleIndex, totalCount) ->
            if (lastVisibleIndex != null && lastVisibleIndex >= totalCount - 2) {
                viewModel.updateFetchReviews(stationId, totalCount.toLong())
                Log.d(
                    "ReviewSync",
                    "launch screen: if launch review screen  $lastVisibleIndex $totalCount "
                )
            }
        }
    }


    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    CustomMainTitle(
                        title = stringResource(R.string.rating_review),
                        modifier = Modifier,
                        fontSize = 24.sp
                    )
                },
                navigationIcon = { BackArrow(onBackClick = { navController.popBackStack() }) }
            )
        },
    ) { innerPadding ->
        station?.let {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
            ) {
                val errorSubmit = stringResource(R.string.failed_submit_review)
                ReviewsSection(
                    avgRating = it.avgRating,
                    reviews = it.reviews,
                    ratingFrequency = viewModel.getRatingFrequency(),
                    isSingleScreen = true,
                    onSubmit = { rating, comment ->
                        Log.d("ReviewsScreen", "Rating: $rating, Comment: $comment")
                        viewModel.submitReview(comment ?: "", rating, onFail = {
                            Toast.makeText(context, errorSubmit, Toast.LENGTH_SHORT).show()
                        })
                    },
                    isGuest = currentUser?.isGuest == true,
                    onTranslate = { review, comment -> viewModel.translate(review, comment) },
                    translations = translations,
                    listState = listState,
                )
            }
        } ?: Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}