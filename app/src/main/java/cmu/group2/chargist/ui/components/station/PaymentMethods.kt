package cmu.group2.chargist.ui.components.station

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cmu.group2.chargist.R
import cmu.group2.chargist.data.model.PaymentMethod
import cmu.group2.chargist.data.model.getIcon
import cmu.group2.chargist.ui.components.common.ItemsRow

@Composable
fun PaymentMethodsRow(
    methods: List<PaymentMethod>,
    alignment: Alignment.Horizontal = Alignment.Start
) {
    ItemsRow(
        items = methods,
        alignment = alignment,
        emptyMessageRes = R.string.no_payment_methods_available,
        contentDescriptionRes = R.string.pay_meth_desc,
        getIcon = { it.getIcon() }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PaymentMethodsSelection(
    selectedMethods: List<PaymentMethod>,
    onSelectionChange: (List<PaymentMethod>) -> Unit
) {
    FlowRow(
        modifier = Modifier
            .wrapContentWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        PaymentMethod.entries.forEach { method ->
            val isSelected = selectedMethods.contains(method)
            val imageRes = method.getIcon()

            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(30.dp))
                    .border(
                        width = if (isSelected) 3.dp else 0.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(36.dp)
                    )
                    .clickable {
                        val newSelection = if (isSelected) {
                            selectedMethods - method
                        } else {
                            selectedMethods + method
                        }
                        onSelectionChange(newSelection)
                    },
                contentAlignment = Alignment.Center
            ) {
                //------- icon img --------------
                Image(
                    painter = imageRes,
                    contentDescription = stringResource(R.string.pay_meth_icon_desc),
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(44.dp)
                )
            }
        }
    }
}