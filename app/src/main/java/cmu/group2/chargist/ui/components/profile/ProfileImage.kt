package cmu.group2.chargist.ui.components.profile

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cmu.group2.chargist.R
import cmu.group2.chargist.ui.components.common.ImageSelectorDialog
import coil.compose.AsyncImage

@Composable
fun ProfileImage(
    picture: String?,
    fallbackImage: Painter,
    onImageSelect: (Uri?) -> Unit,
    editable: Boolean = true
) {
    var showEditDialog by remember { mutableStateOf(false) }

    if (showEditDialog) {
        ImageSelectorDialog(
            title = stringResource(R.string.selected_photo),
            onDismiss = { showEditDialog = false },
            onSelect = { uri ->
                showEditDialog = false
                onImageSelect(uri)
            }
        )
    }

    Box(
        modifier = Modifier.size(100.dp)
    ) {
        AsyncImage(
            model = picture,
            contentDescription = stringResource(R.string.profile_img_desc),
            modifier = Modifier
                .matchParentSize()
                .clip(CircleShape)
                .border(4.dp, MaterialTheme.colorScheme.primary, CircleShape),
            error = fallbackImage,
            fallback = fallbackImage
        )
        if (editable) {
            IconButton(
                onClick = { showEditDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(25.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = stringResource(R.string.edit_img_desc),
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}