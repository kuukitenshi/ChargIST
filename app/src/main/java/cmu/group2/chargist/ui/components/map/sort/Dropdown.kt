package cmu.group2.chargist.ui.components.map.sort

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import cmu.group2.chargist.data.model.SOptions
import cmu.group2.chargist.data.model.getArrowIcon

@Composable
fun Dropdown(
    label: String,
    selectedOption: SOptions,
    options: List<SOptions>,
    onOptionSelected: (SOptions) -> Unit,
    optionLabel: @Composable (SOptions) -> String
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        // -------- sort btn ---------
        OutlinedButton(onClick = { expanded = true }) {
            Text(
                buildAnnotatedString {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("$label: ")
                    }
                    append(optionLabel(selectedOption))
                }
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = selectedOption.getArrowIcon(),
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        }
        // -------- sort options ----------------
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val arrowIcon = option.getArrowIcon()
                            Icon(
                                imageVector = arrowIcon,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(optionLabel(option))
                        }
                    }
                )
            }
        }
    }
}