package com.notnex.myday.ui.screens.mainactivity

import android.content.Intent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.notnex.myday.R
import com.notnex.myday.auth.AuthViewModel
import com.notnex.myday.data.NOT_RATED
import com.notnex.myday.data.ScheduleEntity
import com.notnex.myday.data.TIME_FORMAT
import com.notnex.myday.data.isRated
import com.notnex.myday.ui.CARD_EXPLODE_BOUNDS_KEY
import com.notnex.myday.ui.Settings
import com.notnex.myday.ui.navigation.EditScheduleRoute
import com.notnex.myday.ui.navigation.toDayNoteRoute
import com.notnex.myday.ui.theme.MyDayTheme
import com.notnex.myday.ui.theme.RatingBar
import com.notnex.myday.viewmodel.MyDayViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields

// Полоса недель: 5 недель назад, текущая и 4 вперёд.
private const val WEEKS_BACK = 5
private const val WEEK_PAGES = 10

/**
 * Главный экран собран из небольших функций. Состояние читается здесь, а вниз уходят
 * готовые значения и лямбды: когда меняется расписание, Compose перерисовывает только
 * список, а не полосу недель и шапку.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.MainScreen(
    navController: NavController,
    animatedVisibilityScope: AnimatedVisibilityScope,
    myDayViewModel: MyDayViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val selectedDate by myDayViewModel.selectedDate.collectAsStateWithLifecycle()
    val day by myDayViewModel.selectedDay.collectAsStateWithLifecycle()
    val schedule by myDayViewModel.selectedSchedule.collectAsStateWithLifecycle()
    val authState by authViewModel.authState.collectAsStateWithLifecycle()

    val openSettings = { context.startActivity(Intent(context, Settings::class.java)) }

    SideMenu(
        drawerState = drawerState,
        onOpenSettings = {
            scope.launch { drawerState.close() }
            openSettings()
        },
    ) {
        Scaffold(
            topBar = {
                MainTopBar(
                    avatarUrl = authState.user?.profilePictureUrl,
                    onMenuClick = {
                        scope.launch {
                            if (drawerState.isClosed) drawerState.open() else drawerState.close()
                        }
                    },
                    onAccountClick = openSettings,
                )
            },
            floatingActionButton = {
                CustomFloatingActionButton(
                    navController = navController,
                    selectedDate = selectedDate,
                    fabIcon = Icons.Default.Add
                )
            }
        ) { innerPadding ->
            Column(modifier = Modifier.padding(innerPadding)) {
                WeekStrip(
                    selectedDate = selectedDate,
                    onDateSelected = myDayViewModel::setSelectedDate,
                )

                val note = day?.note.orEmpty()
                ElevatedCard( // текст о дне
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(MyDayTheme.spacing.md)
                        .sharedBounds(
                            sharedContentState = rememberSharedContentState(
                                key = CARD_EXPLODE_BOUNDS_KEY
                            ),
                            animatedVisibilityScope = animatedVisibilityScope
                        )
                        .clickable { navController.navigate(selectedDate.toDayNoteRoute()) }
                ) {
                    Text(
                        text = note.ifEmpty { stringResource(R.string.write_something) },
                        color = if (note.isEmpty()) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(MyDayTheme.spacing.md),
                        maxLines = 10
                    )
                }

                DayRating(
                    rating = day?.score ?: NOT_RATED,
                    onRatingChanged = { myDayViewModel.setRating(selectedDate, it) },
                )

                ScheduleList(
                    schedule = schedule,
                    onItemClick = { navController.navigate(EditScheduleRoute(it.id)) },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainTopBar(
    avatarUrl: String?,
    onMenuClick: () -> Unit,
    onAccountClick: () -> Unit,
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        title = {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleLarge
            )
        },
        navigationIcon = { // кнопка бокового меню
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Default.Menu, contentDescription = stringResource(R.string.menu))
            }
        },
        actions = { // аккаунт: фото профиля или значок-заглушка
            IconButton(onClick = onAccountClick) {
                if (avatarUrl != null) {
                    AsyncImage(
                        model = avatarUrl,
                        contentDescription = stringResource(R.string.profile_picture),
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.AccountCircle,
                        contentDescription = stringResource(R.string.profile_picture),
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
        }
    )
}

/** Листаемая полоса недель; нажатие на день выбирает его. */
@Composable
private fun WeekStrip(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState(initialPage = WEEKS_BACK) { WEEK_PAGES }
    val locale = LocalConfiguration.current.locales[0]
    val today = LocalDate.now()

    // Форматтеры и начало первой недели считаются один раз, а не при каждой перерисовке.
    val dayOfWeekFormatter = remember(locale) { DateTimeFormatter.ofPattern("E", locale) } // «Пн»
    val dayOfMonthFormatter = remember(locale) { DateTimeFormatter.ofPattern("d", locale) } // «26»
    val firstWeekStart = remember(today, locale) {
        // previousOrSame верен и там, где неделя начинается с воскресенья.
        today.with(TemporalAdjusters.previousOrSame(WeekFields.of(locale).firstDayOfWeek))
            .minusWeeks(WEEKS_BACK.toLong())
    }

    HorizontalPager(state = pagerState, modifier = modifier) { page ->
        val weekStart = firstWeekStart.plusWeeks(page.toLong())
        // Равные ячейки (weight) — «таблетка» выбранного дня одной ширины в любом месте недели.
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = MyDayTheme.spacing.sm)) {
            for (offset in 0L..6L) {
                val date = weekStart.plusDays(offset)
                DayCell(
                    dayOfWeek = date.format(dayOfWeekFormatter),
                    dayOfMonth = date.format(dayOfMonthFormatter),
                    isSelected = date == selectedDate,
                    isToday = date == today,
                    onClick = { onDateSelected(date) },
                )
            }
        }
    }
}

@Composable
private fun RowScope.DayCell(
    dayOfWeek: String,
    dayOfMonth: String,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .weight(1f)
            .padding(horizontal = 2.dp)
            .clip(CircleShape)
            .background(
                when {
                    isSelected -> colors.primaryContainer
                    isToday -> colors.secondaryContainer
                    else -> Color.Transparent
                }
            )
            .clickable(onClick = onClick)
            .padding(vertical = MyDayTheme.spacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = dayOfWeek,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) colors.onPrimaryContainer else colors.onSurfaceVariant,
        )
        Text(
            text = dayOfMonth,
            style = MaterialTheme.typography.titleMedium,
            color = if (isSelected) colors.onPrimaryContainer else colors.onSurface,
            modifier = Modifier.padding(top = MyDayTheme.spacing.xxs),
        )
    }
}

@Composable
private fun DayRating(
    rating: Double,
    onRatingChanged: (Double) -> Unit,
) {
    Box(
        modifier = Modifier
            .padding(MyDayTheme.spacing.md)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        RatingBar(
            modifier = Modifier.size(50.dp),
            rating = rating,
            onRatingChanged = onRatingChanged,
            starsColor = when {
                !rating.isRated -> MaterialTheme.colorScheme.outline
                rating >= 4.0 -> Color.Green
                rating >= 2.5 -> colorResource(R.color.orange)
                else -> Color.Red
            }
        )
    }
}

@Composable
private fun ScheduleList(
    schedule: List<ScheduleEntity>,
    onItemClick: (ScheduleEntity) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier) {
        items(items = schedule, key = { it.id }) { item ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(MyDayTheme.spacing.xxs)
                    .clickable { onItemClick(item) }
            ) {
                Text(
                    text = "${item.time.format(TIME_FORMAT)} — ${item.scheduleItem}",
                    modifier = Modifier.padding(MyDayTheme.spacing.md)
                )
            }
        }
    }
}
