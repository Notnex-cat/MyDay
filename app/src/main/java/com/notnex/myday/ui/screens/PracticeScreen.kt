package com.notnex.myday.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.notnex.myday.R
import com.notnex.myday.viewmodel.PracticeViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeScreen(
    navController: NavController
) {

    val practiceViewModel: PracticeViewModel = hiltViewModel()

    var count by rememberSaveable { mutableIntStateOf(0) }

    val counter by practiceViewModel.counter.collectAsStateWithLifecycle()

    val time by practiceViewModel.time.collectAsStateWithLifecycle()

    var noteText by rememberSaveable { mutableStateOf("") }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                title = { Text(stringResource(R.string.practice)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp),
        )
        {
            Row(
                modifier = Modifier
                    .padding(16.dp),
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = count.toString()
                    )
                    Button(
                        { count += 1 }
                    ) {
                        Text(
                            text = "+1"
                        )
                    }
                }


                Column(
                    modifier = Modifier
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = counter.toString()
                    )
                    Button(
                        { practiceViewModel.addCount() }
                    ) {
                        Text(
                            text = "+1"
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                Text(
                    text = time.toString()
                )
                Button(
                    { practiceViewModel.timeCounter() }
                ) {
                    Text(
                        text = "Start"
                    )
                }
            }

            Column(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                TextField(
                    value = noteText,
                    placeholder = { Text(stringResource(R.string.write_something)) },
                    onValueChange = {
                        noteText = it
                        practiceViewModel.onNoteChanged(it)
                    }
                )
                Text(
                    text = "Сохранено: " + practiceViewModel.text.collectAsStateWithLifecycle().value
                )
            }


            val fullSchedule by remember { practiceViewModel.observeNote() }
                .collectAsState(initial = emptyList())

            LazyColumn {
                items(
                    items = fullSchedule,
                    key = { item -> item.date }
                ) { item ->
                    Text(
                        text = "${item.date} — ${item.note} - ${item.mood}",
                        modifier = Modifier.padding(14.dp)
                    )
                    Button(
                        onClick = { practiceViewModel.setMood(item.date, item.mood + 1) }
                    ) {
                        Text(
                            text = "+1"
                        )
                    }
                }
            }
        }
    }
}