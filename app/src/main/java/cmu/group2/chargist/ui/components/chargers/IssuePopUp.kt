package cmu.group2.chargist.ui.components.chargers

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cmu.group2.chargist.R
import cmu.group2.chargist.data.model.ChargerIssue
import cmu.group2.chargist.data.model.toLocalizedName

@Composable
fun IssuePopUp(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (ChargerIssue) -> Unit,
    onRepair: () -> Unit,
    chargerIssue: ChargerIssue
) {
    if (showDialog) {
        val context = LocalContext.current
        var selectedIssue by remember { mutableStateOf<ChargerIssue?>(null) }

        AlertDialog(
            onDismissRequest = onDismiss,
            //-------------title -----------------
            title = {
                Text(
                    text = stringResource(R.string.report_issue),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 20.sp
                )
            },
            text = {
                Column {
                    // -------------- options ---------------
                    Text(stringResource(R.string.select_issue))
                    ChargerIssue.entries.filter { it != ChargerIssue.FINE }.forEach { issue ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedIssue = issue }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = selectedIssue == issue,
                                onClick = { selectedIssue = issue }
                            )
                            Text(
                                text = issue.toLocalizedName(),
                                modifier = Modifier.padding(start = 8.dp),
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))

                    // ------------- report repaired ------------
                    if (chargerIssue != ChargerIssue.FINE) {
                        TextButton(
                            onClick = {
                                onRepair()
                                onDismiss()
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.report_submitted),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }) {
                            Text(
                                stringResource(R.string.report_repaired),
                                color = Color(0xFF4CAF50)
                            )
                        }
                    }
                }
            },
            // ----------- btns cancel/accept --------------------
            confirmButton = {
                Button(
                    onClick = {
                        selectedIssue?.let {
                            onConfirm(it)
                            onDismiss()
                            Toast.makeText(
                                context,
                                context.getString(R.string.report_submitted),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    enabled = selectedIssue != null
                ) {
                    Text(stringResource(R.string.report_issue))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.cancel))
                }
            },
            modifier = Modifier.shadow(12.dp, RoundedCornerShape(20.dp))
        )
    }
}


