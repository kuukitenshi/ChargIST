package cmu.group2.chargist.ui.components.profile

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cmu.group2.chargist.R

@Composable
fun ProfileInfo(
    name: String,
    username: String,
    picture: String?,
    onEditProfileClick: () -> Unit,
    onImageSelect: (Uri?) -> Unit,
    isGuest: Boolean,
    onLogoutOrCreateClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 20.dp,
                end = 20.dp,
                top = 0.dp,
                bottom = 0.dp
            )
    ) {
        ProfileImage(
            picture = picture,
            fallbackImage = painterResource(R.drawable.pfp),
            onImageSelect = onImageSelect,
            editable = !isGuest
        )
        Spacer(modifier = Modifier.width(25.dp))
        // ------------------------- header -----------------------------
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Column(modifier = Modifier.padding(end = 60.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        //------------ name -----------------
                        Text(
                            text = name,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        // ---------------- btn edit profile ----------------------------
                        if (!isGuest) {
                            IconButton(
                                onClick = onEditProfileClick,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = stringResource(R.string.edit_profile),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                    // ----------- username -----------------
                    Text(
                        text = "@${username}",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 16.sp
                        )
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                //------------ create / logout btn ---------------
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 8.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Button(
                        onClick = onLogoutOrCreateClick,
                        content = {
                            Text(
                                text = if (isGuest) stringResource(R.string.create_acc) else stringResource(
                                    R.string.logout
                                )
                            )
                        }
                    )
                }
            }
        }
    }
}