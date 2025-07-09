package cmu.group2.chargist.ui.components.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import cmu.group2.chargist.R
import cmu.group2.chargist.data.api.GeoResponse
import cmu.group2.chargist.data.model.Station

@Composable
fun SearchBarWithClear(
    searchQuery: String,
    stationsList: List<Station>,
    searchResults: List<GeoResponse>,
    onSearchChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    focusRequester: FocusRequester,
    autoFocus: Boolean = false,
    onNavigateToLocation: (Double, Double) -> Unit,
    isInMapScreen: Boolean = true
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    var expanded by remember { mutableStateOf(false) }
    val stationSuffix = "(${stringResource(R.string.station)})"

    LaunchedEffect(autoFocus) {
        if (autoFocus) {
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.large)
                .background(MaterialTheme.colorScheme.surface)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // --------------- search icon --------------------
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = stringResource(R.string.search_desc),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(8.dp))
            //------------------ search bar -------------------------
            TextField(
                value = searchQuery,
                onValueChange = {
                    onSearchChange(it)
                    expanded = it.isNotEmpty()
                },
                placeholder = {
                    Text(
                        stringResource(R.string.search_address),
                        color = Color.Gray
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = Color.Gray,
                    disabledIndicatorColor = Color.LightGray,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedLabelColor = Color.Transparent,
                    unfocusedLabelColor = Color.Transparent,
                    cursorColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    focusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
                //------------------- btn clear ------------------
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = {
                            onClearSearch()
                            expanded = false
                        }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.cancel_search_desc)
                            )
                        }
                    }
                }
            )
        }
        //------------------- suggestions ------------------
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            properties = PopupProperties(focusable = false),
            modifier = Modifier
                .fillMaxWidth(if (isInMapScreen) 0.85f else 0.53f)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                val combined: List<Any> = stationsList + searchResults
                // ----------- empty dropdown ------------------
                if (combined.isEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.loading),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    var index = 0
                    combined.forEach { result ->
                        DropdownMenuItem(
                            text = {
                                when (result) {
                                    is Station -> {
                                        Text(
                                            text = "${result.name} (${stringResource(R.string.station)})",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    is GeoResponse -> Text(
                                        text = result.displayName,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    else -> Text("")
                                }
                            },
                            onClick = {
                                expanded = false
                                when (result) {
                                    is Station -> {
                                        onSearchChange("${result.name} $stationSuffix")
                                        onNavigateToLocation(
                                            result.location.latitude,
                                            result.location.longitude
                                        )
                                    }

                                    is GeoResponse -> {
                                        onSearchChange(result.displayName)
                                        onNavigateToLocation(
                                            result.latitude.toDouble(),
                                            result.longitude.toDouble()
                                        )
                                    }
                                }
                            }
                        )
                        if (index != combined.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 12.dp),
                                thickness = 1.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            )
                        }
                        index++
                    }
                }
            }
        }
    }
}




