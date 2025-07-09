package cmu.group2.chargist.ui.components.reviews

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cmu.group2.chargist.R

@Composable
fun CommentBox(
    commentText: String,
    onTextChange: (String) -> Unit,
    maxChars: Int = 200
) {
    Column {
        Text(stringResource(R.string.write_review_text) + ":")
        BasicTextField(
            value = commentText,
            onValueChange = {
                if (it.length <= maxChars) onTextChange(it)
            },
            textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .heightIn(min = 150.dp)
                .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        )

        //-------- count chars ----------
        Text(
            text = "${commentText.length}/${maxChars}",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.align(Alignment.End)
        )
    }
}
