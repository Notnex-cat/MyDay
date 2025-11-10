package com.notnex.myday.ui.screens.mainactivity

import android.annotation.SuppressLint
import android.content.Intent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.notnex.myday.R
import com.notnex.myday.auth.AuthViewModel
import com.notnex.myday.ui.CARD_EXPLODE_BOUNDS_KEY
import com.notnex.myday.ui.Settings
import com.notnex.myday.ui.theme.RatingBar
import com.notnex.myday.viewmodel.MyDayViewModel
import com.notnex.myday.viewmodel.Screen
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.Locale

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.MainScreen(
    navController: NavController,
    animatedVisibilityScope: AnimatedVisibilityScope,
    myDayViewModel: MyDayViewModel = hiltViewModel()
) {

    val context = LocalContext.current
    val authViewModel: AuthViewModel = hiltViewModel()
    val weekOffsets = rememberPagerState(5, 0f) { 10 }

    val selectedDate by myDayViewModel.selectedDate.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val dayOfWeekFormatter = DateTimeFormatter.ofPattern("E", Locale.getDefault()) // "Пн"
    val dayOfMonthFormatter = DateTimeFormatter.ofPattern("d") // "26"

    val state by authViewModel.authState.collectAsState()

    // Управляем подпиской на облачные обновления в зависимости от аутентификации
    LaunchedEffect(state.isAuthenticated) {
        if (state.isAuthenticated) {
            myDayViewModel.subscribeToUserRealtimeUpdates()
        } else {
            myDayViewModel.stopUserRealtimeUpdates()
        }
    }
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        SideMenu(drawerState)// боковое меню в другом файле
        {
            Scaffold(
                topBar = {
                    CenterAlignedTopAppBar(
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background
                        ),
                        title = { // надпись с названием приложения по середине
                            Text(
                                text = stringResource(R.string.app_name),
                                style = MaterialTheme.typography.titleLarge
                            )
                        },
                        navigationIcon = { // кнопка сендвич для бокового меню
                            IconButton(onClick = {
                                scope.launch {
                                    if (drawerState.isClosed) {
                                        drawerState.open()
                                    } else {
                                        drawerState.close()
                                    }
                                }
                            }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu")
                            }
                        },
                        actions = { // кнопка аккаунт
                            IconButton(onClick = {
                                val intent = Intent(context, Settings::class.java)
                                context.startActivity(intent)
                                //navController.navigate(Screen.SettingScreen.route)
                            }) {
                                val avatarUrl = state.user?.profilePictureUrl

                                if (avatarUrl != null) {
                                    key(avatarUrl) { // Добавляем key для пересоздания при смене URL
                                        AsyncImage(
                                            model = avatarUrl, // Упрощаем без ImageRequest.Builder
                                            contentDescription = "Profile picture",
                                            modifier = Modifier
                                                .size(30.dp)
                                                .clip(CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                } else {
                                    Icon(
                                        imageVector = Icons.Outlined.AccountCircle,
                                        contentDescription = "Default profile icon",
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                            }
                        }
                    )
                },
                floatingActionButton = { //кнопка снизу

                    val isInExpandableScreen =
                        remember { mutableStateOf(true) } // Или derive от NavController

                    CustomFloatingActionButton(
                        navController = navController,
                        selectedDate = selectedDate,
                        expandable = isInExpandableScreen.value,
                        onFabClick = {
                            //Toast.makeText(context, "open", Toast.LENGTH_SHORT).show()
                            // или логика открытия экрана создания события
                        },
                        fabIcon = Icons.Default.Add
                    )
                }
            ) { innerPadding ->
                Column {
                    HorizontalPager(
                        state = weekOffsets,
                        modifier = Modifier.padding(innerPadding)
                    ) { page ->

                        val startOfWeek = LocalDate.now().minusWeeks(5)
                            .with(WeekFields.of(Locale.getDefault()).firstDayOfWeek)
                            .plusWeeks(page.toLong())

                        val weekDates = (0..6).map { startOfWeek.plusDays(it.toLong()) }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            weekDates.forEach { date ->
                                val isSelected =
                                    date == selectedDate // это такие сложные условия оба
                                val isToday = date == LocalDate.now() //

                                Column(
                                    modifier = Modifier
                                        .padding(4.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                            else if (isToday) MaterialTheme.colorScheme.secondaryContainer
                                            else Color.Transparent
                                        )
                                        .clickable { myDayViewModel.setSelectedDate(date) }
                                        .padding(vertical = 6.dp, horizontal = 12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    Text(
                                        text = date.format(dayOfWeekFormatter),
                                        fontSize = 12.sp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                                    )
                                    Text(
                                        text = date.format(dayOfMonthFormatter),
                                        fontSize = 16.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                        }
                    }

                    val fullDB by myDayViewModel.getScore(selectedDate)
                        .collectAsState(initial = null)

                    val fullSchedule by myDayViewModel.getSchedule(selectedDate)
                        .collectAsState(initial = null)

                    val currentRating = fullDB?.score ?: 4.5

                    val text = fullDB?.note ?: ""

                    val currentDateState = rememberUpdatedState(selectedDate)
                    val currentfullDBState = rememberUpdatedState(fullDB) //это прям объект всей БД

                    Column {
                        ElevatedCard( // текст о дне
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = 2.dp
                            ),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.onTertiary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .sharedBounds(
                                    sharedContentState = rememberSharedContentState(
                                        key = CARD_EXPLODE_BOUNDS_KEY
                                    ),
                                    animatedVisibilityScope = animatedVisibilityScope
                                )
                                .clickable {
                                    navController.navigate("${Screen.DayNote.route}/${selectedDate}")
                                }

                        ) {
                            Text(
                                text = text.ifEmpty { stringResource(R.string.write_something) },
                                color = if (text.isEmpty()) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(16.dp),
                                maxLines = 10
                            )
                        }
                        Box(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            RatingBar( // рейтинг дня
                                modifier = Modifier
                                    .size(50.dp),
                                rating = currentRating,
                                onRatingChanged = { newRating ->
                                    val date = currentDateState.value
                                    val noteText = currentfullDBState.value?.note.orEmpty()
                                    val aiResponseText =
                                        currentfullDBState.value?.aiFeedback.orEmpty()
                                    myDayViewModel.saveDayEntry(
                                        date,
                                        newRating,
                                        noteText,
                                        aiResponseText
                                    )
                                },
                                starsColor = when {
                                    currentRating >= 4.0 -> Color.Green
                                    currentRating >= 2.5 -> colorResource(R.color.orange)
                                    else -> Color.Red
                                }
                            )
                        }
                        Text(
                            text = fullSchedule.toString()
                        )
                    }
                }
            }
        }
    }
}

