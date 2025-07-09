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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import cmu.group2.chargist.viewmodel.RegisterErrorType
import cmu.group2.chargist.viewmodel.RegisterState
import cmu.group2.chargist.viewmodel.RegisterViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(navController: NavController, viewModel: RegisterViewModel = viewModel()) {
    val context = LocalContext.current
    val name by viewModel.name.collectAsState()
    val username by viewModel.username.collectAsState()
    val password by viewModel.password.collectAsState()
    val confirmPassword by viewModel.confirmPassword.collectAsState()
    val passwordValid by viewModel.isPasswordValid.collectAsState()
    val matchingPassword by viewModel.doPasswordsMatch.collectAsState()
    val registerState by viewModel.registerState.collectAsState()
    val favourites by viewModel.favorites.collectAsState()

    val alreadyExistsError = stringResource(R.string.already_acc)

    LaunchedEffect(registerState) {
        if (registerState is RegisterState.Success) {
            navController.navigate(Screens.Map.route) {
                popUpTo(Screens.Opening.route) { inclusive = true }
            }
        } else if (registerState is RegisterState.Error) {
            val message = when ((registerState as RegisterState.Error).type) {
                RegisterErrorType.USER_ALREADY_EXISTS -> alreadyExistsError
                RegisterErrorType.UNKNOWN -> "An error creating account"
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
                    title = stringResource(R.string.register_button),
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 24.dp)
                )
                // -------- fields -----------------------
                CustomTextField(
                    value = name,
                    onValueChange = { viewModel.updateName(it) },
                    label = stringResource(R.string.name)
                )
                Spacer(modifier = Modifier.height(8.dp))

                CustomTextField(
                    value = username,
                    onValueChange = { viewModel.updateUsername(it) },
                    label = stringResource(R.string.username_field)
                )
                Spacer(modifier = Modifier.height(8.dp))

                PasswordConfirmationFields(
                    password = password,
                    confirmPassword = confirmPassword,
                    onPasswordChange = { viewModel.updatePassword(it) },
                    onConfirmPasswordChange = { viewModel.updateConfirmPassword(it) },
                    passwordValid = passwordValid,
                    matchingPassword = matchingPassword,
                    spaceBtwFields = 8
                )
                Spacer(modifier = Modifier.height(20.dp))

                // ------------------- button ------------------------
                CustomButton(
                    text = if (registerState !is RegisterState.Idle) stringResource(R.string.register_button_loading) else stringResource(
                        R.string.register_button
                    ),
                    onClick = {
                        if (viewModel.isFormValid()) {
                            viewModel.register()
                        }
                    },
                    enabled = viewModel.isFormValid(),
                    modifier = Modifier.fillMaxWidth(0.8f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    modifier = Modifier.fillMaxWidth(0.8f),
                    onClick = { navController.navigate(Screens.Login.route) }
                ) {
                    Text(stringResource(R.string.already_acc))
                }
            }
        }
    )
}

@Composable
fun PasswordConfirmationFields(
    password: String,
    confirmPassword: String,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    passwordValid: Boolean,
    matchingPassword: Boolean,
    passwordLabel: String = stringResource(R.string.new_pass),
    spaceBtwFields: Int = 8,
    width: Float = 0.8f,
) {
    val errorColor = Color(0xFFC93E48)

    Column(
        verticalArrangement = Arrangement.Top,
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ---------- new password -----------------
        CustomTextField(
            value = password,
            onValueChange = { onPasswordChange(it) },
            label = passwordLabel,
            width = width,
            showPasswordToggle = true
        )
        // --- validation error ----
        if (password.isNotEmpty() && !passwordValid) {
            Text(
                text = stringResource(R.string.pass_error),
                color = errorColor,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .padding(top = 5.dp)
                    .fillMaxWidth(width)
            )
        }
        Spacer(modifier = Modifier.height(spaceBtwFields.dp))

        // --------- confirm password-----------------
        CustomTextField(
            value = confirmPassword,
            onValueChange = { onConfirmPasswordChange(it) },
            label = stringResource(R.string.confirm_pass),
            width = width,
            showPasswordToggle = true
        )
        // ----- matching error --------
        if (confirmPassword.isNotEmpty() && !matchingPassword) {
            Text(
                text = stringResource(R.string.pass_not_match),
                color = errorColor,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .padding(top = 5.dp)
                    .fillMaxWidth(width)
            )
        }
    }
}