package com.notnex.myday.ui.screens.settingsactivity

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.notnex.myday.R
import com.notnex.myday.neuralnetwork.AiProvider
import com.notnex.myday.ui.theme.AppIcons
import com.notnex.myday.ui.theme.MyDayTheme
import com.notnex.myday.viewmodel.AiSettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiSettingsScreen(
    navController: NavController,
    aiSettingsViewModel: AiSettingsViewModel
) {
    val selected by aiSettingsViewModel.selectedAi.collectAsStateWithLifecycle(AiProvider.GEMINI)

    var expanded by remember { mutableStateOf(false) }

    val withKey by aiSettingsViewModel.providersWithKey.collectAsStateWithLifecycle(emptySet())

    val hasKey = selected in withKey

    var keyInput by remember { mutableStateOf("") }

    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                title = { Text(stringResource(R.string.ai_settings)) },
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
                .padding(MyDayTheme.spacing.md)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(MyDayTheme.spacing.sm)
        ) {
            ExposedDropdownMenuBox(
                expanded,
                onExpandedChange = {
                    expanded = !expanded
                }
            ) {
                TextField(
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth(),
                    readOnly = true,
                    value = selected.displayName,
                    onValueChange = { },
                    label = { Text(stringResource(R.string.ai_provider)) },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded = expanded
                        )
                    },
                    colors = ExposedDropdownMenuDefaults.textFieldColors()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = {
                        expanded = false
                    }
                ) {
                    AiProvider.entries.forEach { provider ->
                        DropdownMenuItem(
                            text = { Text(provider.displayName) },
                            trailingIcon = {
                                if (provider in withKey) Icon(
                                    AppIcons.Key,
                                    contentDescription = null
                                )
                            },
                            onClick = {
                                aiSettingsViewModel.selectProvider(provider)
                                expanded = false
                            }
                        )
                    }
                }
            }
            Text(
                text = stringResource(R.string.not_supported_countries),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
            if (!hasKey) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = keyInput,
                    onValueChange = { keyInput = it },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        autoCorrectEnabled = false
                    ),
                    label = { Text(stringResource(R.string.api_key)) },
                )
                Button(
                    enabled = keyInput.isNotBlank(),
                    onClick = {
                        aiSettingsViewModel.saveApiKey(selected, keyInput)
                        keyInput = ""
                    }
                ) {
                    Text(stringResource(R.string.save))
                }
            } else {
                Text(
                    text = stringResource(R.string.key_saved)
                )
                Button(
                    onClick = { aiSettingsViewModel.clearApiKey(selected) }
                ) {
                    Text(stringResource(R.string.delete))
                }
            }
        }
    }
}