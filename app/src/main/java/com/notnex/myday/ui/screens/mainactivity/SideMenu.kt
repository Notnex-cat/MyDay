package com.notnex.myday.ui.screens.mainactivity

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Abc
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.DrawerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.notnex.myday.R
import com.notnex.myday.ui.theme.MyDayTheme

@Composable
fun SideMenu(
    drawerState: DrawerState,
    onOpenSettings: () -> Unit,
    onOpenPractice: () -> Unit,
    content: @Composable () -> Unit
) {
    ModalNavigationDrawer(
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(285.dp),
                drawerContainerColor = MaterialTheme.colorScheme.background,
                drawerContentColor = MaterialTheme.colorScheme.onBackground,
                drawerTonalElevation = 6.dp
            ) {
                Column(modifier = Modifier.padding(horizontal = MyDayTheme.spacing.md)) {
                    Spacer(Modifier.height(MyDayTheme.spacing.sm))
                    Text(
                        stringResource(R.string.app_name),
                        modifier = Modifier.padding(MyDayTheme.spacing.md),
                        style = MaterialTheme.typography.titleLarge
                    )
                    HorizontalDivider()
                    Spacer(Modifier.height(MyDayTheme.spacing.xs))
                    NavigationDrawerItem(
                        label = { Text(stringResource(R.string.settings)) },
                        selected = false,
                        icon = { Icon(Icons.Outlined.Settings, contentDescription = null) },
                        onClick = onOpenSettings
                    )
                    Spacer(Modifier.height(MyDayTheme.spacing.xs))
                    NavigationDrawerItem(
                        label = { Text(stringResource(R.string.practice))},
                        selected = false,
                        icon = { Icon(Icons.Outlined.Abc, contentDescription = null) },
                        onClick = onOpenPractice
                    )
                }
            }
        },
        drawerState = drawerState,
        content = content
    )
}
