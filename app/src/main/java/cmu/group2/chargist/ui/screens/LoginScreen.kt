package cmu.group2.chargist.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import cmu.group2.chargist.R
import cmu.group2.chargist.Screens
import cmu.group2.chargist.ui.components.common.BackArrow
import cmu.group2.chargist.ui.components.common.custom.CustomButton
import cmu.group2.chargist.ui.components.common.custom.CustomMainTitle
import cmu.group2.chargist.ui.components.common.custom.CustomTextField
import cmu.group2.chargist.viewmodel.LoginState
import cmu.group2.chargist.viewmodel.LoginViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController, viewModel: LoginViewModel = viewModel()) {
    val loginState by viewModel.loginState.collectAsState()
    val username by viewModel.username.collectAsState()
    val password by viewModel.password.collectAsState()
    val context = LocalContext.current
    val errorLogin = stringResource(R.string.error_couldnt_login)

    // Navigate when login is successful
    LaunchedEffect(loginState) {
        if (loginState is LoginState.Success) {
            navController.navigate(Screens.Map.route) {
                popUpTo(Screens.Opening.route) { inclusive = true }
            }
        } else if (loginState is LoginState.Error) {
            // Handle error state
            val message = when ((loginState as LoginState.Error).type) {
                else -> errorLogin
            }
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        // ---------- back nav bar ---------------
        topBar = {
            CenterAlignedTopAppBar(
                title = { Spacer(modifier = Modifier) },
                navigationIcon = { BackArrow(onBackClick = { navController.popBackStack() }) }
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // -------- title ----------------------------
                CustomMainTitle(
                    title = stringResource(R.string.login_button),
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 24.dp)
                )

                // -------- fields ----------------------
                CustomTextField(
                    value = username,
                    onValueChange = { viewModel.updateUsername(it) },
                    label = stringResource(R.string.username_field)
                )
                Spacer(modifier = Modifier.height(8.dp))

                CustomTextField(
                    value = password,
                    onValueChange = { viewModel.updatePassword(it) },
                    label = stringResource(R.string.password_field),
                    showPasswordToggle = true
                )
                Spacer(modifier = Modifier.height(20.dp))

                // --------- buttons -----------------------------
                CustomButton(
                    text = if (loginState !is LoginState.Idle) stringResource(R.string.login_button_loading) else stringResource(
                        R.string.login_button
                    ),
                    onClick = { viewModel.login() },
                    enabled = viewModel.isFormValid(),
                    modifier = Modifier.fillMaxWidth(0.8f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    modifier = Modifier.fillMaxWidth(0.8f),
                    onClick = { navController.navigate(Screens.Register.route) })
                {
                    Text(stringResource(R.string.new_here))
                }
            }
        }
    )
}