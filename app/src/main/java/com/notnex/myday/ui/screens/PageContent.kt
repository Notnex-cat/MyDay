package com.notnex.myday.ui.screens

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.notnex.myday.viewmodel.MyDayViewModel

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun PageContent(
    navController: NavController,
    onCardClick: () -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope,
    viewModel: MyDayViewModel = hiltViewModel()
) {

}