package cmu.group2.chargist.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import cmu.group2.chargist.R
import cmu.group2.chargist.data.model.ChargerPower
import cmu.group2.chargist.data.model.ChargerStatus
import cmu.group2.chargist.data.model.ChargerType
import cmu.group2.chargist.ui.components.chargers.ChargerBundleItem
import cmu.group2.chargist.ui.components.common.BackArrow
import cmu.group2.chargist.ui.components.common.EmptyStateMessage
import cmu.group2.chargist.ui.components.common.custom.CustomMainTitle
import cmu.group2.chargist.viewmodel.ChargerDetailsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChargerDetailsScreen(
    stationId: Long,
    bundleType: String,
    bundlePower: String,
    navController: NavController,
    viewModel: ChargerDetailsViewModel = viewModel()
) {
    val context = LocalContext.current
    viewModel.fetchStation(stationId)
    viewModel.updateFilter { charger ->
        charger.type == ChargerType.valueOf(bundleType) && charger.power == ChargerPower.valueOf(
            bundlePower
        )
    }
    val bundles by viewModel.bundles.collectAsState()

    val errorChangeStatus = stringResource(R.string.failed_change_status)
    val errorReportIssue = stringResource(R.string.report_issue)

    Scaffold(
        // ---------- top bar chargers ------------------
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    CustomMainTitle(
                        title = stringResource(R.string.chargers),
                        modifier = Modifier
                    )
                },
                navigationIcon = { BackArrow(onBackClick = { navController.popBackStack() }) }
            )
        },
        content = { innerPadding ->
            if (bundles.isNotEmpty()) {
                bundles.forEach { bundle ->
                    val totalChargers = bundle.amount

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .padding(innerPadding)
                    ) {
                        Spacer(modifier = Modifier.height(8.dp))
                        //--------------- title -----------------
                        val availableChargers =
                            bundle.chargers.count { it.status == ChargerStatus.FREE }
                        Text(
                            text = buildAnnotatedString {
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append(stringResource(R.string.availability) + ": ")
                                }
                                append("$availableChargers/$totalChargers")
                            },
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        //---------- scrollable cards ---------------
                        LazyColumn {
                            items(bundle.chargers) { charger ->
                                ChargerBundleItem(
                                    charger = charger,
                                    onToggleStatus = { status ->
                                        viewModel.updateChargerStatus(
                                            charger,
                                            status,
                                            onFail = { Toast.makeText(context, errorChangeStatus, Toast.LENGTH_SHORT).show() }
                                        )
                                    },
                                    onReportIssue = { issue ->
                                        viewModel.reportChargerIssue(
                                            charger,
                                            issue,
                                            onFail = { Toast.makeText(context, errorReportIssue, Toast.LENGTH_SHORT).show() }
                                        )
                                    },
                                    onRepair = {
                                        viewModel.repairCharger(charger, onFail = {
                                            Toast.makeText(context, errorChangeStatus, Toast.LENGTH_SHORT).show()
                                        })
                                    }
                                )
                            }
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyStateMessage(
                        icon = Icons.Outlined.CloudDownload,
                        message = stringResource(R.string.loading_bundle_details)
                    )
                }
            }
        }
    )
}