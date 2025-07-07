package com.notnex.myday.ui.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.android.gms.auth.api.identity.Identity
import com.notnex.myday.R
import com.notnex.myday.auth.AuthViewModel

@Composable
fun AuthScreen(
    navController: NavController
) {
    val viewModel: AuthViewModel = androidx.hilt.navigation.compose.hiltViewModel()
    val authState by viewModel.authState.collectAsState()
    val emailState = remember { mutableStateOf("") }
    val passwordState = remember { mutableStateOf("") }
    val context = LocalContext.current
    val oneTapClient = remember { Identity.getSignInClient(context) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
        onResult = { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intent = result.data ?: return@rememberLauncherForActivityResult
                viewModel.signInWithGoogle(intent)
            }
        }
    )

    LaunchedEffect(authState.isAuthenticated) {
        if (authState.isAuthenticated) {
            navController.popBackStack()
        }
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (authState.isLoading) {
                CircularProgressIndicator()
            }
            if (authState.error != null) {
                Text(text = authState.error ?: "", color = Color.Red)
                Button(onClick = { viewModel.clearError() }) { Text("Очистить ошибку") }
            }
            TextField(
                modifier = Modifier.border(width = 2.dp, color = Color.DarkGray, shape = RoundedCornerShape(20.dp)),
                label = { Text(text = stringResource(R.string.login)) },
                value = emailState.value, onValueChange = { emailState.value = it }
            )
            Spacer(modifier = Modifier.height(10.dp))
            TextField(
                modifier = Modifier
                    .border(width = 2.dp, color = Color.DarkGray, shape = RoundedCornerShape(20.dp)),
                label = {
                    Text(text = stringResource(R.string.password)
                    )
                        },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                value = passwordState.value, onValueChange = { passwordState.value = it }
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(onClick = {
                viewModel.signInWithEmail(emailState.value, passwordState.value)
                }
            ) {
                Text(text = stringResource(R.string.sign_in))
            }
            Button(onClick = {
                viewModel.signUpWithEmail(emailState.value, passwordState.value)
                }
            ) {
                Text(text = stringResource(R.string.sign_up))
            }
            Button(onClick = {
                viewModel.signOut()
                }
            ) {
                Text(text = stringResource(R.string.sign_out))
            }

            Button(onClick = {
                oneTapClient.beginSignIn(
                    com.google.android.gms.auth.api.identity.BeginSignInRequest.Builder()
                        .setGoogleIdTokenRequestOptions(
                            com.google.android.gms.auth.api.identity.BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
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
            }) {
                Text(text = "Google sign in")
            }
        }
    }
}