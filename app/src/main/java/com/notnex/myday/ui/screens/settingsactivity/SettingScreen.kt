package com.notnex.myday.ui.screens.settingsactivity

import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.collectAsState
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
import com.notnex.myday.notification.ReminderType
import com.notnex.myday.ui.navigation.AiSettingsRoute
import com.notnex.myday.ui.navigation.AuthRoute
import com.notnex.myday.ui.theme.MyDayTheme
import com.notnex.myday.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: AuthState,
    onSignOut: () -> Unit,
    navController: NavController,
    settingsViewModel: SettingsViewModel
    ) {

    val context = LocalContext.current
    val activity = LocalActivity.current

    var showMenu by remember { mutableStateOf(false) }
    // Для какого напоминания открыто окно выбора времени; null — окно закрыто.
    // Одна переменная вместо Boolean на каждое напоминание: «открыты два окна сразу» не записать.
    var timePickerType by remember { mutableStateOf<ReminderType?>(null) }

    val dayNoteEnabled by settingsViewModel.dayNoteEnabled.collectAsState(false)
    val dayNoteTime by settingsViewModel.dayNoteTime.collectAsState(null)

    val bedtimeEnabled by settingsViewModel.bedtimeEnabled.collectAsState(false)
    val bedtimeTime by settingsViewModel.bedtimeTime.collectAsState(null)

    val aiEnabled by settingsViewModel.aiEnabled.collectAsState(false)
    val hasApiKey by settingsViewModel.hasApiKey.collectAsState(true)
    val aiKeyMissing = aiEnabled && !hasApiKey

    LaunchedEffect(key1 = state.error) {
        state.error?.let { error ->
            Toast.makeText(
                context, error, Toast.LENGTH_LONG
            ).show()
        }
    }

    timePickerType?.let { type ->
        ReminderTimeDialog(
            if (type == ReminderType.DAY_NOTE) dayNoteTime else bedtimeTime,
            onConfirm = { time ->
                settingsViewModel.enableReminder(time, type)
                timePickerType = null
            },
            onDismiss = { timePickerType = null },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ), title = { Text(stringResource(R.string.settings)) }, navigationIcon = {
                    // экран стартовый в своей активити: popBackStack оставил бы пустое окно
                    IconButton(onClick = { activity?.finish() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                })
        },
    ) { innerPadding ->
        Column(modifier = Modifier
            .padding(innerPadding)
            .verticalScroll(rememberScrollState())) {
            ElevatedCard(
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 8.dp
                ), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.onTertiary
                ), modifier = Modifier.Companion
                    .fillMaxWidth()
                    .padding(MyDayTheme.spacing.md, bottom = MyDayTheme.spacing.xs, end = MyDayTheme.spacing.md)
                    .clickable {
                        if (state.user == null) {
                            navController.navigate(AuthRoute)
                        }
                    }) {
                if (state.user != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        state.user.profilePictureUrl?.let { url ->
                            AsyncImage(
                                model = url,
                                contentDescription = stringResource(R.string.profile_picture),
                                modifier = Modifier
                                    .size(80.dp)
                                    .padding(MyDayTheme.spacing.md)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.height(MyDayTheme.spacing.md))
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

                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.TopEnd
                        ) {
                            IconButton(onClick = { showMenu = !showMenu }) {
                                Icon(
                                    Icons.Default.MoreVert,
                                    contentDescription = stringResource(R.string.menu)
                                )
                            }

                            DropdownMenu(
                                expanded = showMenu, onDismissRequest = { showMenu = false }) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.sign_out)) },
                                    onClick = {
                                        onSignOut()
                                        showMenu = false
                                    })
                            }
                        }

                    }
                } else {
                    Text(
                        text = stringResource(R.string.sign_in),
                        textAlign = TextAlign.Center,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            //переключатель напоминания о заметке
            SettingsSwitchRow(
                subtitle = dayNoteTime?.toString() ?: "",
                title = R.string.notification_channel_name,
                enabled = dayNoteEnabled,
                onClick = { timePickerType = ReminderType.DAY_NOTE },
                onEnabledChange = { isOn ->
                    if (isOn) {
                        // Переключатель возвращает прежнее время; пока DataStore не прочитан (null), ждём.
                        dayNoteTime?.let {
                            settingsViewModel.enableReminder(
                                it, ReminderType.DAY_NOTE
                            )
                        }
                    } else {
                        settingsViewModel.disableReminder(ReminderType.DAY_NOTE)
                    }
                },
            )
            // переключатель время спать
            SettingsSwitchRow(
                subtitle = bedtimeTime?.toString() ?: "",
                title = R.string.time_to_sleep,
                enabled = bedtimeEnabled,
                onClick = { timePickerType = ReminderType.BEDTIME },
                onEnabledChange = { isOn ->
                    if (isOn) {
                        bedtimeTime?.let {
                            settingsViewModel.enableReminder(
                                it, ReminderType.BEDTIME
                            )
                        }
                    } else {
                        settingsViewModel.disableReminder(ReminderType.BEDTIME)
                    }
                },
            )
            //переключатель  для ии
            SettingsSwitchRow(
                title = R.string.ai_settings,
                subtitle = stringResource(if (aiKeyMissing) R.string.no_api_key else R.string.ai_settings_subtitle),
                enabled = aiEnabled,
                isError = aiKeyMissing,
                onClick = { navController.navigate(AiSettingsRoute) },
                onEnabledChange = { isOn -> settingsViewModel.enableAI(isOn) },
            )
        }
    }
}

