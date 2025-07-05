package com.notnex.myday.ui.screens

import android.annotation.SuppressLint
import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.notnex.myday.R
import com.notnex.myday.firebase.SignInState
import com.notnex.myday.firebase.UserData

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("ContextCastToActivity", "SuspiciousIndentation")
@Composable
fun SettingsScreen(
    state: SignInState,
    onSignInClick: () -> Unit,
    userData: UserData?,
    onSignOut: () -> Unit
){
    val context = LocalContext.current as? Activity

    LaunchedEffect(key1 = state.signInError) {
        state.signInError?.let { error ->
            Toast.makeText(
                context,
                error,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = {
                        //navController.popBackStack()
                        context?.finish()
                        }
                    ) {// завершение активности
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },

        ) { innerPadding ->
        Column(modifier = Modifier.Companion.padding(innerPadding)) {
            ElevatedCard( //аккаунт
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 2.dp
                ),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.onTertiary
                ),
                modifier = Modifier.Companion
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clickable {
                        if (userData == null) {
                            onSignInClick()
                        } else {
                            //преход на какой-нибудь экранчик сделать надо с аккаунтом
                        }
                    }
            ) {
                if (userData != null) {
                    Row(
                       // modifier = Modifier.padding(16.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        userData.profilePictureUrl?.let { url ->
                            AsyncImage(
                                model = url,
                                contentDescription = "Profile picture",
                                modifier = Modifier
                                    .size(70.dp)
                                    .padding(16.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        userData.username?.let { name ->
                            Text(
                                text = name,
                                textAlign = TextAlign.Center,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.SemiBold,
                            )

                            Button(
                                modifier = Modifier.padding(16.dp),
                                onClick = onSignOut
                            ) {
                                Text(text = "Sign out")
                            }
                        }
                    }
                } else {
                    Text(
                        text = "Sign in",
                        textAlign = TextAlign.Center,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
