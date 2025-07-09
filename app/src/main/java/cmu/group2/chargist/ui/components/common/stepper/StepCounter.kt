package cmu.group2.chargist.ui.components.common.stepper

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cmu.group2.chargist.R

@Composable
fun StepCounter(
    currentStep: Int,
    steps: List<Step>,
    canProceed: (Int) -> Boolean,
    completedSteps: Set<Int>,
    onStepClick: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        //--------------- circles ------------
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (step in 1..steps.size) {
                StepCircle(
                    step = step,
                    currentStep = currentStep,
                    canProceed = canProceed,
                    completedSteps = completedSteps,
                    onClick = { onStepClick(step) }
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        // -------- title circle -------------
        Text(
            text = steps[currentStep - 1].title,
            fontSize = 23.sp,
            style = MaterialTheme.typography.headlineLarge.copy(
                color = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.padding(bottom = 4.dp),
        )
    }
}

@Composable
private fun StepCircle(
    step: Int,
    currentStep: Int,
    canProceed: (Int) -> Boolean,
    completedSteps: Set<Int>,
    onClick: () -> Unit
) {
    val isDone = completedSteps.contains(step)
    val isSelected = step == currentStep
    val validProceed = !completedSteps.contains(currentStep) || canProceed(currentStep)
    val canClick =
        (isDone || step == currentStep + 1 && completedSteps.contains(currentStep)) && validProceed

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(32.dp)
            .then(
                if (isSelected) Modifier.background(MaterialTheme.colorScheme.primary, CircleShape)
                else Modifier
            )
            .padding(1.dp)
            .then(
                if (canClick) Modifier.clickable { onClick() }
                else Modifier
            )
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(
                    when {
                        isDone || isSelected -> Color(0xFF5ca462)
                        else -> Color.LightGray
                    }
                )
        ) {
            if (isDone) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = stringResource(R.string.done),
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text(
                    text = "$step",
                    color = Color.White,
                    fontSize = 12.sp
                )
            }
        }
    }
}
