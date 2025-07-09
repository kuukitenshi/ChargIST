package cmu.group2.chargist.ui.components.reviews

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cmu.group2.chargist.R
import coil.compose.AsyncImage
import java.text.DateFormat
import java.util.Date

@Composable
fun ReviewCard(
    profilePicture: String?,
    name: String,
    username: String,
    rating: Int,
    date: Date,
    originalComment: String,
    onTranslateClick: () -> Unit,
    translatedComment: String?
) {
    val starColor = Color(0xFFFFC107)
    var showTranslation by remember { mutableStateOf(false) }
    val df = DateFormat.getDateInstance(DateFormat.SHORT)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = if (originalComment.isNotBlank()) 4.dp else 0.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {

                //----------profile pic------------
                AsyncImage(
                    model = profilePicture,
                    placeholder = painterResource(id = R.drawable.pfp),
                    error = painterResource(id = R.drawable.pfp),
                    contentDescription = stringResource(R.string.profile_img_desc),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    //---------- username ---------
                    Text(
                        text = buildAnnotatedString {
                            pushStyle(
                                SpanStyle(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                            append(name)
                            pop()
                            append("  ")
                            pushStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurfaceVariant))
                            append("@$username")
                            pop()
                        },
                        style = MaterialTheme.typography.titleSmall
                    )

                    //------------ rating -------------------
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        repeat(rating) {
                            Icon(
                                Icons.Filled.Star,
                                contentDescription = null,
                                tint = starColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        repeat(5 - rating) {
                            Icon(
                                Icons.Outlined.Star,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        //--------------- date ---------------------------
                        Text(df.format(date), fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }
            if (originalComment.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                //------------- comment ----------------------------
                Text(
                    text = if (showTranslation && translatedComment != null) translatedComment else originalComment,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                //------------- translate ----------------------------
                Text(
                    text = if (showTranslation && translatedComment != null) stringResource(R.string.orignal) else stringResource(
                        R.string.translate
                    ),
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .align(Alignment.Start)
                        .clickable {
                            showTranslation = !showTranslation
                            onTranslateClick.invoke()
                        }
                )
            }
        }
    }
}
