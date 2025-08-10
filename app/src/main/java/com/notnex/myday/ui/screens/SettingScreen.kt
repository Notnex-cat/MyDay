package com.notnex.myday.ui.screens

import android.annotation.SuppressLint
import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.notnex.myday.R
import com.notnex.myday.auth.AuthState

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("ContextCastToActivity", "SuspiciousIndentation")
@Composable
fun SettingsScreen(
    state: AuthState,
    onSignOut: () -> Unit,
    navController: NavController
){
    val context = LocalContext.current as? Activity
    var showMenu by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = state.error) {
        state.error?.let { error ->
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
                        context?.finish()
                        }
                    ) {
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
            ElevatedCard(
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 8.dp
                ),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.onTertiary
                ),
                modifier = Modifier.Companion
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clickable {
                        if (state.user == null) {
                            navController.navigate(com.notnex.myday.viewmodel.Screen.Auth.route)
                        }
                    }
            ) {
                if (state.user != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        state.user.profilePictureUrl?.let { url ->
                            AsyncImage(
                                model = url,
                                contentDescription = "Profile picture",
                                modifier = Modifier
                                    .size(80.dp)
                                    .padding(16.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        Column {
                            state.user.username?.let { name ->
                                Text(
                                    text = name,
                                    textAlign = TextAlign.Center,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                            state.user.email?.let { email ->
                                Text(
                                    text = email,
                                    textAlign = TextAlign.Center,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }

                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopEnd) {
                            IconButton(onClick = { showMenu = !showMenu }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Меню")
                            }

                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.sign_out)) },
                                    onClick = {
                                        onSignOut()
                                        showMenu = false
                                    }
                                )
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
