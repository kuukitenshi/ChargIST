package cmu.group2.chargist.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import cmu.group2.chargist.R
import cmu.group2.chargist.Screens
import cmu.group2.chargist.popAll
import cmu.group2.chargist.viewmodel.OpeningViewModel

@Composable
fun OpeningScreen(navController: NavController, viewModel: OpeningViewModel = viewModel()) {
    val logo =
        if (isSystemInDarkTheme()) painterResource(R.drawable.chargist_opening) else painterResource(
            R.drawable.chargist_opening_light_2
        )
    val currentUser by viewModel.currentUser.collectAsState()

    LaunchedEffect(currentUser) {
        currentUser?.let {
            navController.navigate(Screens.Map.route)
        }
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = logo,
                contentDescription = "logoApp",
                modifier = Modifier.size(350.dp)//.background(Color.Yellow)
            )

            // ------------- buttons ---------------
            LoginRegisterButton(
                text = stringResource(R.string.login_button),
                onClick = { navController.navigate(Screens.Login.route) }
            )

            LoginRegisterButton(
                text = stringResource(R.string.register_button),
                onClick = { navController.navigate(Screens.Register.route) }
            )

            //------------ guest button ------------
            TextButton(
                modifier = Modifier.fillMaxWidth(0.5f),
                onClick = {
                    viewModel.continueAsGuest()
                    navController.navigate(Screens.Map.route) {
                        navController.popAll()
                    }
                })
            { Text(stringResource(R.string.continue_as_guest)) }
        }
    }
}

@Composable
fun LoginRegisterButton(
    text: String,
    onClick: () -> Unit
) {
    Button(
        modifier = Modifier.fillMaxWidth(0.5f),
        onClick = onClick
    ) {
        Text(text = text)
    }
    Spacer(modifier = Modifier.padding(8.dp))
}
