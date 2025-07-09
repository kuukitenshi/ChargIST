package cmu.group2.chargist.ui.components.common

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import cmu.group2.chargist.R
import cmu.group2.chargist.createTmpFile

@Composable
fun ImageSelectorDialog(
    title: String,
    onDismiss: () -> Unit,
    onSelect: (Uri?) -> Unit
) {
    val context = LocalContext.current
    val tmpFile = context.createTmpFile("jpg")
    val capturedUri =
        FileProvider.getUriForFile(context, "${context.packageName}.provider", tmpFile)

    val galleryLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            onSelect(uri)
            onDismiss()
        }

    val cameraLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { taken ->
            if (taken) {
                onSelect(capturedUri)
                onDismiss()
            }
        }

    val denied = stringResource(R.string.permission_denied)
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        if (it) {
            cameraLauncher.launch(capturedUri)
        } else {
            Toast.makeText(context, denied, Toast.LENGTH_SHORT).show()
        }
    }

    //--------------- popup -------------------------
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 8.dp,
            modifier = Modifier
                .padding(16.dp)
                .shadow(12.dp, RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // -------------- title ------------------
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))

                //----------- take ---------------------
                Button(
                    onClick = {
                        val permissionCheckResult =
                            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                        if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) {
                            cameraLauncher.launch(capturedUri)
                        } else {
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    Text(stringResource(R.string.take_photo))
                }
                Spacer(modifier = Modifier.height(6.dp))
                //----------- divider ---------------------
                OrDivider()
                //----------- gallery ---------------------
                Button(
                    onClick = { galleryLauncher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    Text(stringResource(R.string.pick_gallery))
                }
                Spacer(modifier = Modifier.height(4.dp))

                // --------- cancel btn -------------------
                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth(0.8f)) {
                    Text(stringResource(R.string.cancel))
                }
            }
        }
    }

}
