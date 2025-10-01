package com.notnex.myday.ui.screens.settingsactivity

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.notnex.myday.R
import com.notnex.myday.auth.AuthViewModel
import com.notnex.myday.viewmodel.MyDayViewModel
import com.notnex.myday.viewmodel.Screen

@Composable
fun AuthScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel(),
    myDayViewModel: MyDayViewModel = hiltViewModel()
) {
    val authState by authViewModel.authState.collectAsState()
    val emailState = remember { mutableStateOf("") }
    val passwordState = remember { mutableStateOf("") }
    val context = LocalContext.current
    val oneTapClient = remember { Identity.getSignInClient(context) }
    //val auth = remember { Firebase.auth }

    val focusManager = LocalFocusManager.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
        onResult = { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intent = result.data ?: return@rememberLauncherForActivityResult
                authViewModel.signInWithGoogle(intent)
            }
        }
    )

    LaunchedEffect(authState.isAuthenticated) {
        if (authState.isAuthenticated) {
            navController.navigate(Screen.SettingScreen.route) {
                popUpTo(Screen.SettingScreen.route) { inclusive = true }
                launchSingleTop = true
                myDayViewModel.syncLocalDataToCloud()
            }
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
            if (authState.isLoading) {
                CircularProgressIndicator()
            }

            TextField(
                modifier = Modifier.border(width = 2.dp, color = Color.DarkGray, shape = RoundedCornerShape(20.dp)),
                label = { Text(text = stringResource(R.string.login)) },
                maxLines = 1,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = {
                        focusManager.moveFocus(FocusDirection.Down)
                    }
                ),
                value = emailState.value, onValueChange = { emailState.value = it }
            )
            Spacer(modifier = Modifier.height(10.dp))
            TextField(
                modifier = Modifier
                    .border(width = 2.dp, color = Color.DarkGray, shape = RoundedCornerShape(20.dp)),
                label = { Text(text = stringResource(R.string.password))},
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                maxLines = 1,
                value = passwordState.value, onValueChange = { passwordState.value = it }
            )
            if (authState.error != null) {
                Text(text = stringResource(R.string.log_in_error), color = Color.Red)
                //Button(onClick = { viewModel.clearError() }) { Text("Очистить ошибку") }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(onClick = {
                authViewModel.signInWithEmail(emailState.value, passwordState.value)
                },
                shape = RoundedCornerShape(12.dp),

                modifier = Modifier
                    .height(52.dp)
            ) {
                Text(text = stringResource(R.string.sign_in))
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(onClick = {
                authViewModel.signUpWithEmail(emailState.value, passwordState.value)
                },
                shape = RoundedCornerShape(12.dp),

                modifier = Modifier
                    .height(52.dp)
            ) {
                Text(text = stringResource(R.string.sign_up))
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(onClick = {
                oneTapClient.beginSignIn(
                    BeginSignInRequest.Builder()
                        .setGoogleIdTokenRequestOptions(
                            BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                                .setSupported(true)
                                .setFilterByAuthorizedAccounts(false)
                                .setServerClientId(context.getString(R.string.web_client_id))
                                .build()
                        )
                        .setAutoSelectEnabled(true)
                        .build()
                ).addOnSuccessListener { result ->
                    launcher.launch(
                        IntentSenderRequest.Builder(result.pendingIntent.intentSender).build()
                    )
                }
            },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier
                    .height(52.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.google_icon_logo), // добавь иконку
                        contentDescription = "Google Sign-In",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = stringResource(R.string.sign_in_with_google))
                }
            }
        }
    }
}