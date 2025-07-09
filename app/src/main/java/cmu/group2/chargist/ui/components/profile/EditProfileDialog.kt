package cmu.group2.chargist.ui.components.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cmu.group2.chargist.R
import cmu.group2.chargist.ui.components.common.custom.CustomTextField

@Composable
fun EditProfileDialog(
    startName: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    maxNameLength: Int = 12
) {
    var name by remember { mutableStateOf(startName) }
    var isNameValid by remember { mutableStateOf(true) }

    val validateName = { newName: String ->
        isNameValid = newName.length <= maxNameLength && newName.isNotEmpty()
    }
    val errorColor = Color(0xFFC93E48)

    LaunchedEffect(name) {
        validateName(name)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        //------------ title -----------------
        title = {
            Text(
                text = stringResource(R.string.edit_profile),
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
            )
        },
        // --------------field --------------------
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CustomTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = stringResource(R.string.name),
                    width = 0.9f,
                    modifier = Modifier.fillMaxWidth(0.9f)
                )
                if (!(name.length <= maxNameLength && name.isNotEmpty())) {
                    Text(
                        text = stringResource(R.string.exceed_size_name),
                        color = errorColor,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .fillMaxWidth(0.9f)
                    )
                }
            }
        },
        confirmButton = {
            val isValid = name.length <= maxNameLength && name.isNotEmpty()
            Button(
                onClick = { if (isValid) onSave(name) },
                enabled = isValid && name != startName
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
        modifier = Modifier
            .padding(16.dp)
            .shadow(12.dp, RoundedCornerShape(20.dp))
    )
}