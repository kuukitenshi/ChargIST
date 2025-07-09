package cmu.group2.chargist.ui.components.reviews

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.RateReview
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cmu.group2.chargist.R
import cmu.group2.chargist.data.model.Review
import cmu.group2.chargist.ui.components.common.EmptyStateMessage

@Composable
fun ReviewsSection(
    avgRating: Float,
    reviews: List<Review>,
    ratingFrequency: Map<Int, Int>,
    isSingleScreen: Boolean = false,
    onSubmit: (Int, String?) -> Unit,
    onTranslate: (Review, String) -> Unit,
    isGuest: Boolean,
    translations: Map<Review, String>,
    listState: LazyListState = rememberLazyListState(),
    maxToShow: Int = 3,
    onViewMore: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(horizontal = if (isSingleScreen) 6.dp else 0.dp)
    ) {
        // --------------- title ------------
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = if (isSingleScreen) 16.dp else 0.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (isSingleScreen) {
                Spacer(Modifier.width(8.dp))
            } else {
                Text(
                    text = stringResource(R.string.rating_review),
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 20.sp),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
            }
            // ------------- pop up review --------------
            if (!isSingleScreen) {
                CommentButtonPopUp(onSubmit = onSubmit, isGuest = isGuest)
            }
        }
        Spacer(Modifier.height(8.dp))

        // ------------ histogram ------------------
        RatingHistogram(
            averageRating = avgRating,
            totalReviews = reviews.size,
            ratingCounts = ratingFrequency
        )
        Spacer(Modifier.height(12.dp))

        // ------------ view more ---------------
        if (!isSingleScreen && onViewMore != null) {
            TextButton(
                onClick = onViewMore,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(
                    text = stringResource(R.string.view_all_reviews),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.height(4.dp))
        }

        // ------------- pop up review / write review btn --------------
        if (isSingleScreen) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CommentButtonPopUp(
                    onSubmit = onSubmit,
                    isGuest = isGuest
                )
                Spacer(Modifier.height(4.dp))
            }
        }

        //---------- review cards ---------------
        if (reviews.isEmpty()) {
            EmptyStateMessage(
                icon = Icons.Outlined.RateReview,
                message = stringResource(R.string.no_reviews)
            )
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (isSingleScreen) Modifier.weight(1f)
                        else Modifier.heightIn(min = 200.dp, max = 500.dp)
                    )
            ) {
                items(if (!isSingleScreen) reviews.take(maxToShow) else reviews) {
                    ReviewCard(
                        profilePicture = it.user.pictureUrl,
                        name = it.user.name,
                        username = it.user.username,
                        rating = it.rating,
                        date = it.date,
                        originalComment = it.comment,
                        onTranslateClick = { onTranslate(it, it.comment) },
                        translatedComment = translations[it],
                    )
                }
            }
        }
    }
}
