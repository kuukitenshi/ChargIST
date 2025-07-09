package cmu.group2.chargist.ui.components.common.custom

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit

@Composable
fun CustomMainTitle(
    title: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = MaterialTheme.typography.headlineLarge.fontSize,
) {
    Text(
        text = title,
        style = MaterialTheme.typography.headlineLarge.copy(
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            fontSize = fontSize
        ),
        modifier = modifier
    )
}