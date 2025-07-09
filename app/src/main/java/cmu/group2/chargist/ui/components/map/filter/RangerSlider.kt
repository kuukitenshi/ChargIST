package cmu.group2.chargist.ui.components.map.filter

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun RangeSlider(
    range: ClosedFloatingPointRange<Float>,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (ClosedFloatingPointRange<Float>) -> Unit,
    valueFormatter: (ClosedFloatingPointRange<Float>) -> String,
    steps: Int
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        RangeSlider(
            value = range,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps
        )
        Text(valueFormatter(range), color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}