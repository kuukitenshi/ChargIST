package cmu.group2.chargist.ui.components.reviews

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cmu.group2.chargist.R
import cmu.group2.chargist.ui.components.common.custom.CustomButton

@Composable
fun CommentButtonPopUp(
    onSubmit: (Int, String?) -> Unit,
    isGuest: Boolean
) {
    var openDialog by remember { mutableStateOf(false) }
    var commentText by remember { mutableStateOf("") }
    var rating by remember { mutableIntStateOf(0) }
    val context = LocalContext.current

    // -------------- btn review ---------
    val guest = stringResource(R.string.review_requires_account)
    ButtonReview(onClick = {
        if (isGuest) {
            Toast.makeText(context, guest, Toast.LENGTH_LONG).show()
        } else {
            openDialog = true
        }
    })
    // -------------- pop up review ----------------------
    if (openDialog) {
        AlertDialog(
            onDismissRequest = { openDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.write_review),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 20.sp
                )
            },
            text = {
                Column {
                    // ---------- star bar and box ---------
                    RatingStarBar(rating = rating, onRatingChanged = { rating = it })
                    CommentBox(
                        commentText = commentText,
                        onTextChange = { commentText = it }
                    )
                }
            },
            // ------------ submit btn ------------------
            confirmButton = {
                CustomButton(
                    text = stringResource(R.string.submit),
                    onClick = {
                        if (rating > 0) {
                            onSubmit(rating, commentText)
                            rating = 0
                            commentText = ""
                            openDialog = false

                            Toast.makeText(
                                context,
                                context.getString(R.string.review_sent_text),
                                Toast.LENGTH_SHORT
                            ).show()

                        }
                    },
                    enabled = rating > 0,
                )
            },
            dismissButton = {
                TextButton(onClick = { openDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            modifier = Modifier.shadow(12.dp, RoundedCornerShape(20.dp))
        )
    }
}
