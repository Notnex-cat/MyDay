package com.notnex.myday.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.notnex.myday.R
import com.notnex.myday.ui.Settings
import com.notnex.myday.viewmodel.MyDayViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.Locale

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavController,
    viewModel: MyDayViewModel = hiltViewModel()
    ) {

    val context = LocalContext.current
    val weekOffsets = rememberPagerState(5, 0f) { 10 }

    var selectedDate by rememberSaveable { mutableStateOf(LocalDate.now()) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val dayOfWeekFormatter = DateTimeFormatter.ofPattern("E", Locale.getDefault()) // "Пн"
    val dayOfMonthFormatter = DateTimeFormatter.ofPattern("d") // "26"

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        SideMenu(drawerState)// боковое меню в другом файле
        {
            Scaffold(
                topBar = {
                    TopAppBar(
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background
                        ),
                        title = { // надпись с названием приложения по середине
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(R.string.app_name),
                                    style = MaterialTheme.typography.titleLarge
                                )

                            }
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
                                //navController.navigate(Screen.Settings.route)
                            }) {
                                Icon(Icons.Outlined.AccountCircle, contentDescription = "Account")
                            }
                        }
                    )
                },
                floatingActionButton = { //кнопка снизу
                    FloatingActionButton(onClick = {
                        Toast.makeText(
                            context,
                            "Добавить заметку",
                            Toast.LENGTH_SHORT
                        ).show()
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Добавить заметку")
                    }
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
                                        .clickable { selectedDate = date }
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
                    PageContent(
                        selectedDate,
                        navController,
                        viewModel
                    ) // текст дня и рейтинг дня все что ниже даты
                }
            }
        }
    }
}