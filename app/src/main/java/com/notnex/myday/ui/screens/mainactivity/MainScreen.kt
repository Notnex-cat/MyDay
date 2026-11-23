package com.notnex.myday.ui.screens.mainactivity

import android.content.Intent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material3.DrawerValue
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
import androidx.compose.runtime.key
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
import com.notnex.myday.ui.theme.ratingColor
import com.notnex.myday.viewmodel.MyDayViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import kotlin.time.Duration.Companion.seconds

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
    val now by produceState(LocalTime.now()) {
        while (true) {
            delay(60.seconds)
            value = LocalTime.now()
        }
    }

    SideMenu(
        drawerState = drawerState,
        onOpenSettings = {
            scope.launch { drawerState.close() }
            openSettings()
        },
    ) {
        Scaffold(topBar = {
            MainTopBar(
                avatarUrl = authState.user?.profilePictureUrl,
                onMenuClick = {
                    scope.launch {
                        if (drawerState.isClosed) drawerState.open() else drawerState.close()
                    }
                },
                onAccountClick = openSettings,
            )
        }, floatingActionButton = {
            CustomFloatingActionButton(
                navController = navController,
                selectedDate = selectedDate,
                fabIcon = Icons.Default.Add
            )
        }) { innerPadding ->
            // Тема могла смениться, пока экран был в фоне (в настройках). Уже созданные элементы
            // ленивого списка тогда не всегда получают новые цвета — часть карточек оставалась
            // в старой теме. Ключ по палитре пересоздаёт список целиком; цена — прокрутка наверх.
            key(MyDayTheme.palette) {
                LazyColumn(modifier = Modifier.padding(innerPadding)) {
                    item(key = "week") {
                        WeekStrip(
                            selectedDate = selectedDate,
                            onDateSelected = myDayViewModel::setSelectedDate,
                        )
                    }
                    val note = day?.note.orEmpty()
                    val aiFeedback = day?.aiFeedback.orEmpty()
                    item(key = "rating") {
                        MainCard {
                            DayRating(
                                rating = day?.score ?: NOT_RATED,
                                onRatingChanged = { myDayViewModel.setRating(selectedDate, it) },
                            )
                        }
                    }
                    item(key = "note") {
                        MainCard(
                            // У заголовка titleMedium высокая строка, над буквами уже есть воздух.
                            topPadding = MyDayTheme.spacing.xs,
                            modifier = Modifier
                                .sharedBounds(
                                    sharedContentState = rememberSharedContentState(
                                        key = CARD_EXPLODE_BOUNDS_KEY
                                    ), animatedVisibilityScope = animatedVisibilityScope
                                )
                                .clickable { navController.navigate(selectedDate.toDayNoteRoute()) },
                        ) {
                            Text(
                                text = stringResource(R.string.day_note_title),
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(
                                text = note.ifEmpty { stringResource(R.string.write_something) },
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (note.isEmpty()) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(top = MyDayTheme.spacing.xs),
                            )
                        }
                    }
                    if (aiFeedback.isNotEmpty()) {
                        item(key = "assistant") {
                            MainCard(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                modifier = Modifier.clickable { navController.navigate(selectedDate.toDayNoteRoute()) },
                            ) {
                                Text(
                                    text = "✦ " + stringResource(R.string.assistant_title),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                )
                                Text(
                                    text = aiFeedback.withoutMarkdown(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(top = MyDayTheme.spacing.xs),
                                )
                            }
                        }
                    }
                    item {
                        Text(
                            modifier = Modifier.padding(
                                start = MyDayTheme.spacing.md, end = MyDayTheme.spacing.md
                            ),
                            text = stringResource(R.string.schedule),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    scheduleItems(
                        date = selectedDate,
                        time = now,
                        schedule = schedule,
                        onItemClick = { navController.navigate(EditScheduleRoute(it.id)) })
                }
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
        ), title = {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleLarge
            )
        }, navigationIcon = { // кнопка бокового меню
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Default.Menu, contentDescription = stringResource(R.string.menu))
            }
        }, actions = { // аккаунт: фото профиля или значок-заглушка
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
        })
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MyDayTheme.spacing.sm)
        ) {
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

/**
 * Содержимое карточки оценки: подпись, звёзды и оценка цифрой справа. Сама карточка —
 * MainCard снаружи, поэтому здесь нет ни фона, ни внешних отступов.
 */
@Composable
private fun DayRating(
    rating: Double,
    onRatingChanged: (Double) -> Unit,
) {
    Text(
        text = stringResource(R.string.how_day_gone),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = MyDayTheme.spacing.xs),
    ) {
        // weight(1f) у звёзд: они занимают всё свободное место, и цифра уходит к правому краю.
        RatingBar(
            modifier = Modifier.weight(1f),
            rating = rating,
            onRatingChanged = onRatingChanged,
            starsColor = MyDayTheme.palette.ratingColor(rating)
                ?: MaterialTheme.colorScheme.outlineVariant
        )
        Text(
            // Целая оценка без «.0»: «4», а не «4.0»; не оценено — прочерк.
            text = when {
                !rating.isRated -> "—"
                rating % 1.0 == 0.0 -> rating.toInt().toString()
                else -> rating.toString()
            },
            style = MaterialTheme.typography.headlineSmall,
        )
    }
}

private fun LazyListScope.scheduleItems(
    date: LocalDate,
    time: LocalTime,
    schedule: List<ScheduleEntity>,
    onItemClick: (ScheduleEntity) -> Unit
) {
    val today = LocalDate.now()
    val currentIndex = schedule.indexOfLast { !it.time.isAfter(time) }
    if (schedule.isEmpty()) {
        item {
            Text(
                modifier = Modifier.padding(
                    start = MyDayTheme.spacing.md,
                    end = MyDayTheme.spacing.md,
                    top = MyDayTheme.spacing.xs,
                    bottom = MyDayTheme.spacing.xs
                ),
                text = stringResource(R.string.schedule_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        itemsIndexed(schedule, key = { index, item -> item.id }) { index, item ->
            val state = when {
                date.isBefore(today) -> ItemState.Past
                date.isAfter(today) -> ItemState.Future
                index < currentIndex -> ItemState.Past
                index == currentIndex -> ItemState.Present
                else -> ItemState.Future
            }

            val dotColor = when (state) {
                ItemState.Past -> MaterialTheme.colorScheme.outlineVariant
                ItemState.Present -> MaterialTheme.colorScheme.primaryContainer
                ItemState.Future -> MaterialTheme.colorScheme.outline
            }
            val dotSize = if (state == ItemState.Present) 14.dp else 10.dp
            val boldTime = if (state == ItemState.Present) FontWeight.Bold else FontWeight.Normal
            val boldText =
                if (state == ItemState.Present) FontWeight.SemiBold else FontWeight.Normal

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .height(IntrinsicSize.Min)
                    .fillMaxWidth()
                    .padding(start = MyDayTheme.spacing.md, end = MyDayTheme.spacing.md)
                    .clickable { onItemClick(item) }) {
                Text(
                    text = item.time.format(TIME_FORMAT),
                    modifier = Modifier.width(56.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = boldTime,
                    textAlign = TextAlign.Center
                )
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        Modifier.fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(if (index == 0) Color.Transparent else MaterialTheme.colorScheme.outlineVariant)
                                .width(2.dp)


                        )
                        Box(
                            Modifier
                                .size(dotSize)
                                .clip(CircleShape)
                                .background(dotColor)

                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(if (index == schedule.lastIndex) Color.Transparent else MaterialTheme.colorScheme.outlineVariant)
                                .width(2.dp)


                        )
                    }
                }
                Text(
                    text = item.scheduleItem,
                    modifier = Modifier
                        .padding(
                            top = MyDayTheme.spacing.sm,
                            bottom = MyDayTheme.spacing.sm,
                            start = MyDayTheme.spacing.xs,
                            end = MyDayTheme.spacing.xs
                        )
                        .weight(1f),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = boldText
                )
                if (state == ItemState.Present) {
                    Box(
                        Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .padding(start = MyDayTheme.spacing.sm,
                                end = MyDayTheme.spacing.sm,
                                top = MyDayTheme.spacing.xxs,
                                bottom = MyDayTheme.spacing.xxs)

                    ) {
                        Text(
                            stringResource(R.string.now),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }
    }
}

// Нейросеть отвечает с разметкой Markdown; в превью из трёх строк звёздочки и решётки — мусор.
private fun String.withoutMarkdown(): String =
    replace(Regex("[*_`#]+"), "").replace(Regex("\\s+"), " ").trim()

enum class ItemState { Past, Present, Future }