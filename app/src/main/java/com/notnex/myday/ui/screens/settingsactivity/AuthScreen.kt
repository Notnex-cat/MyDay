package com.notnex.myday.ui.screens.settingsactivity

import android.widget.Toast
import androidx.activity.compose.LocalActivity
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.notnex.myday.R
import com.notnex.myday.auth.AuthViewModel
import com.notnex.myday.ui.theme.MyDayTheme
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    val emailState = rememberSaveable { mutableStateOf("") }
    val passwordState = remember { mutableStateOf("") }
    val context = LocalContext.current
    // Окно выбора аккаунта Google показывается поверх активити, поэтому нужна она, а не контекст приложения.
    val activity = LocalActivity.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }
    //val auth = remember { Firebase.auth }

    val focusManager = LocalFocusManager.current

    // Синхронизацией занимается SyncManager по факту входа — экрану остаётся только вернуться назад
    LaunchedEffect(authState.isAuthenticated) {
        if (authState.isAuthenticated) navController.popBackStack()
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
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = {
                        focusManager.moveFocus(FocusDirection.Down)
                    }
                ),
                value = emailState.value, onValueChange = { emailState.value = it }
            )
            Spacer(modifier = Modifier.height(MyDayTheme.spacing.sm))
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

            Spacer(modifier = Modifier.height(MyDayTheme.spacing.sm))

            Button(onClick = {
                authViewModel.signInWithEmail(emailState.value, passwordState.value)
                },
                shape = RoundedCornerShape(12.dp),
                enabled = !authState.isLoading,

                modifier = Modifier
                    .height(52.dp)
            ) {
                Text(text = stringResource(R.string.sign_in))
            }

            Spacer(modifier = Modifier.height(MyDayTheme.spacing.sm))

            Button(onClick = {
                authViewModel.signUpWithEmail(emailState.value, passwordState.value)
                },
                shape = RoundedCornerShape(12.dp),
                enabled = !authState.isLoading,

                modifier = Modifier
                    .height(52.dp)
            ) {
                Text(text = stringResource(R.string.sign_up))
            }

            Spacer(modifier = Modifier.height(MyDayTheme.spacing.sm))

            Button(onClick = {
                val host = activity ?: return@Button
                scope.launch {
                    try {
                        val result = credentialManager.getCredential(
                            host,
                            GetCredentialRequest.Builder()
                                .addCredentialOption(
                                    GetSignInWithGoogleOption.Builder(context.getString(R.string.web_client_id)).build()
                                )
                                .build()
                        )
                        val idToken = result.credential.googleIdTokenOrNull()
                        if (idToken != null) {
                            authViewModel.signInWithGoogle(idToken)
                        } else {
                            Toast.makeText(context, R.string.google_sign_in_error, Toast.LENGTH_LONG).show()
                        }
                    } catch (e: GetCredentialCancellationException) {
                        // Человек сам закрыл окно выбора аккаунта — это не ошибка.
                    } catch (e: GetCredentialException) {
                        // Чаще всего на телефоне нет аккаунта Google или сервисов Google Play.
                        // Без сообщения кнопка молча не делала бы ничего.
                        Toast.makeText(context, R.string.google_sign_in_error, Toast.LENGTH_LONG).show()
                    }
                }
            },
                shape = RoundedCornerShape(12.dp),
                enabled = !authState.isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
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
                        contentDescription = stringResource(R.string.sign_in_with_google),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(MyDayTheme.spacing.sm))
                    Text(text = stringResource(R.string.sign_in_with_google))
                }
            }
        }
    }
}

/** Токен Google из ответа Credential Manager; null, если пришло что-то другое или ответ не разобрать. */
private fun Credential.googleIdTokenOrNull(): String? {
    if (this !is CustomCredential || type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) return null
    return try {
        GoogleIdTokenCredential.createFrom(data).idToken
    } catch (e: GoogleIdTokenParsingException) {
        null
    }
}
