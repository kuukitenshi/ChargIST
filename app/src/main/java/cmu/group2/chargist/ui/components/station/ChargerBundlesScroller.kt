package cmu.group2.chargist.ui.components.station

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cmu.group2.chargist.data.model.ChargerBundle
import cmu.group2.chargist.data.model.ChargerPower
import cmu.group2.chargist.data.model.ChargerType
import cmu.group2.chargist.ui.components.chargers.ChargerBundleCard
import cmu.group2.chargist.ui.components.common.PageIndicatorHorizontalScroll

@Composable
fun ChargerBundlesScroller(
    bundles: List<ChargerBundle>,
    onBundleClick: (ChargerBundle) -> Unit
) {
    val ccs2Bundles = bundles.filter { it.type == ChargerType.CCS2 }
    val type2Bundles = bundles.filter { it.type == ChargerType.TYPE2 }

    val sortByPower = { list: List<ChargerBundle> ->
        list.sortedBy {
            when (it.power) {
                ChargerPower.FAST -> 0
                ChargerPower.MEDIUM -> 1
                ChargerPower.SLOW -> 2
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        //---------------cc2-------------
        if (ccs2Bundles.isNotEmpty()) {
            ChargerBundlePagerSection(
                bundles = sortByPower(ccs2Bundles),
                onBundleClick = onBundleClick
            )
        }
        // ----------- type 2 ------------
        if (type2Bundles.isNotEmpty()) {
            ChargerBundlePagerSection(
                bundles = sortByPower(type2Bundles),
                onBundleClick = onBundleClick
            )
        }
    }
}


@Composable
private fun ChargerBundlePagerSection(
    bundles: List<ChargerBundle>,
    onBundleClick: (ChargerBundle) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { bundles.size })
    // --------- section title ----------
    Spacer(Modifier.height(2.dp))
    // ------------- scroller
    HorizontalPager(state = pagerState) { page ->
        val bundle = bundles[page]
        ChargerBundleCard(
            chargerBundle = bundle,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .width(500.dp),
            onClick = onBundleClick
        )
    }
    // --------- balls indicator ---------
    PageIndicatorHorizontalScroll(
        pagerState = pagerState,
        totalItems = bundles.size
    )
}