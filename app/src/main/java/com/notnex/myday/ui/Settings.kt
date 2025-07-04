package com.notnex.myday.ui

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.notnex.myday.R
import com.notnex.myday.ui.theme.MyDayTheme
import com.notnex.myday.viewmodel.MyDayViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class Settings() : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController() // если нужен
            val viewModel: MyDayViewModel = hiltViewModel()

            MyDayTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Scaffold(
                        topBar = {
                            TopAppBar(
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.background
                                ),
                                title = { Text(stringResource(R.string.settings)) },
                                navigationIcon = {
                                    IconButton(onClick = {(this as? Activity)?.finish()}) {// завершение активности
                                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                                    }
                                }
                            )

                        },

                        ) { innerPadding ->
                        Column(modifier = Modifier.padding(innerPadding)) {
                            ElevatedCard( //аккаунт
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
                                    .clickable {
                                        //navController.navigate("${Screen.DayNote.route}/${selectedDate}/${currentRating}/${text}") //преход на полный экран с описанием дня
                                    }
                            ) {
                                Text(
                                    text = "Account",
                                    //color = if (text.isEmpty()) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier.padding(16.dp),
                                    maxLines = 10
                                )

                            }
                        }
                    }
                }
            }
        }
    }
}

