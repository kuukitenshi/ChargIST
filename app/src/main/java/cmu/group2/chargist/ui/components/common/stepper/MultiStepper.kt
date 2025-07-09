package cmu.group2.chargist.ui.components.common.stepper

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cmu.group2.chargist.R
import cmu.group2.chargist.ui.components.common.custom.CustomButton

data class Step(
    val title: String,
    val content: @Composable () -> Unit
)

@Composable
fun MultiStepper(steps: List<Step>, onComplete: () -> Unit, canProceed: (Int) -> Boolean) {
    var currentStep by remember { mutableIntStateOf(1) }
    var completedSteps by remember { mutableStateOf(emptySet<Int>()) }
    Scaffold(
        bottomBar = {
            BottomFixedBar(
                currentStep = currentStep,
                stepCount = steps.size,
                onNext = {
                    completedSteps = completedSteps + currentStep
                    currentStep++
                },
                onDone = onComplete,
                onBack = { currentStep-- },
                canProceed = canProceed(currentStep)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            StepCounter(
                currentStep = currentStep,
                steps = steps,
                canProceed = canProceed,
                completedSteps = completedSteps,
                onStepClick = { currentStep = it })
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                steps[currentStep - 1].content()
            }
        }
    }
}

@Composable
private fun BottomFixedBar(
    currentStep: Int,
    stepCount: Int,
    onNext: () -> Unit,
    onBack: () -> Unit,
    onDone: () -> Unit,
    canProceed: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // --------- back opt button ------------
        if (currentStep > 1) {
            CustomButton(
                text = stringResource(id = R.string.back),
                onClick = onBack,
                enabled = true
            )
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }

        // --------- next or done button ------------
        when (currentStep) {
            in 1..stepCount - 1 -> {
                CustomButton(
                    text = stringResource(id = R.string.next),
                    onClick = onNext,
                    enabled = canProceed
                )
            }

            stepCount -> {
                CustomButton(
                    text = stringResource(id = R.string.confirm_save),
                    onClick = onDone,
                    enabled = canProceed
                )
            }
        }
    }
}
