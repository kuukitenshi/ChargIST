package cmu.group2.chargist.ui.components.station.form

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import cmu.group2.chargist.R
import cmu.group2.chargist.data.model.Charger
import cmu.group2.chargist.data.model.ChargerBundle
import cmu.group2.chargist.data.model.ChargerPower
import cmu.group2.chargist.data.model.ChargerType
import cmu.group2.chargist.ui.components.common.custom.CustomButton
import cmu.group2.chargist.ui.components.common.custom.CustomTextField
import cmu.group2.chargist.ui.components.map.filter.TypeChargerButton

@Composable
fun AddChargerPopUp(
    existingBundles: List<ChargerBundle>,
    onAddCharger: (ChargerBundle) -> Unit,
    onDismiss: () -> Unit,
    title: String,
    bundleToEdit: ChargerBundle? = null
) {
    val alreadyExistsType = stringResource(R.string.already_exits_charger_type)
    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        content = {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp)
                    .shadow(8.dp, RoundedCornerShape(20.dp))
            ) {
                AddChargerPopUpContent(
                    onCancel = onDismiss,
                    title = title,
                    bundleToEdit = bundleToEdit,
                    onConfirm = { plugType, power, count, price ->
                        val available = if (bundleToEdit != null) {
                            val diff = count - bundleToEdit.amount
                            (bundleToEdit.available + diff).coerceAtLeast(0)
                        } else {
                            count
                        }
                        val newCharger = ChargerBundle(
                            type = ChargerType.valueOf(plugType),
                            power = ChargerPower.valueOf(power),
                            price = price.toDouble(),
                            amount = count,
                            available = available,
                            chargers = List(count) {
                                Charger(
                                    id = 0,
                                    type = ChargerType.valueOf(plugType),
                                    power = ChargerPower.valueOf(power),
                                    price = price.toDouble(),
                                )
                            }
                        )

                        if (bundleToEdit != null) { // edit charger bundle
                            onAddCharger(newCharger)
                            onDismiss()
                        } else { // add charger bundle
                            val exists = existingBundles.any {
                                it.type == newCharger.type && it.power == newCharger.power && it.price == newCharger.price
                            }
                            if (exists) {
                                Toast.makeText(context, alreadyExistsType, Toast.LENGTH_SHORT)
                                    .show()
                            } else {
                                onAddCharger(newCharger)
                                onDismiss()
                            }
                        }
                    }
                )
            }
        }
    )
}

@Composable
private fun AddChargerPopUpContent(
    onConfirm: (String, String, Int, Float) -> Unit,
    onCancel: () -> Unit,
    title: String,
    bundleToEdit: ChargerBundle? = null
) {
    val context = LocalContext.current

    var selectedType by remember(bundleToEdit) {
        mutableStateOf(bundleToEdit?.type?.name)
    }
    var selectedPower by remember(bundleToEdit) {
        mutableStateOf(bundleToEdit?.power?.name)
    }
    var amount by remember(bundleToEdit) {
        mutableFloatStateOf(bundleToEdit?.amount?.toFloat() ?: 1f)
    }
    var price by remember(bundleToEdit) {
        mutableStateOf(
            bundleToEdit?.price?.let { "%.2f".format(it) } ?: "0.50"
        )
    }

    Box(modifier = Modifier.padding(18.dp)) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            //---------- title ------------------
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            )
            Spacer(Modifier.height(16.dp))

            // ------------ plug type ---------
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.plug_type),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                ChargerType.entries
                    .forEach { type ->
                        TypeChargerButton(
                            type = type,
                            isSelected = selectedType == type.name,
                            onClick = { selectedType = type.name }
                        )
                    }
            }
            Spacer(Modifier.height(16.dp))

            // ------------------ power -----------------------
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.power),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                ChargerPower.entries.forEach { power ->
                    val isSelected = selectedPower == power.name
                    PowerTagButton(
                        power = power,
                        selected = isSelected,
                        onClick = { selectedPower = power.name }
                    )
                }
            }
            Spacer(Modifier.height(16.dp))

            // ---------------- amount -------------------
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.amount),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(8.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Slider(
                    value = amount,
                    onValueChange = { amount = it },
                    valueRange = 1f..30f,
                    steps = 30
                )
                Text(
                    text = amount.toInt().toString(),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(16.dp))

            //------------------ price ----------------
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.filter_option_price) + " (€ per kWh)",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                // -------- minus ------
                IconButton(onClick = {
                    val value = price.toFloatOrNull() ?: 0f
                    if (value > 0.1f) price = "%.2f".format(value - 0.1f)
                }) {
                    Icon(
                        Icons.Default.Remove,
                        contentDescription = stringResource(R.string.reduce_price),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                // --- field ---------
                CustomTextField(
                    value = price,
                    onValueChange = {
                        price = it
                        it.toFloatOrNull()?.let { parsed -> price = price.toString() }
                    },
                    width = 0.5f
                )
                // ------------- plus ---------------
                IconButton(onClick = {
                    val value = price.toFloatOrNull() ?: 0f
                    price = "%.2f".format(value + 0.1f)
                }) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = stringResource(R.string.increase_price),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(42.dp))

            // ------------ btns -------------------
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                //---------- cancel ------------
                TextButton(onClick = onCancel) {
                    Text(stringResource(R.string.cancel))
                }
                // -------- confirm -----------
                CustomButton(
                    text = stringResource(R.string.confirm),
                    onClick = {
                        selectedType?.let { type ->
                            selectedPower?.let { power ->
                                val parsedPrice = price.toFloatOrNull()
                                if (parsedPrice != null) {
                                    onConfirm(type, power, amount.toInt(), parsedPrice)
                                }
                            }
                        }
                    },
                    enabled = selectedType != null && selectedPower != null && price.toFloatOrNull() != null
                )
            }
        }
    }
}