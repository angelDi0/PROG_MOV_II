
@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.myapplication.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import com.example.myapplication.ui.screens.HomeScreen
import com.example.myapplication.ui.screens.LoginScreen
import com.example.myapplication.viewmodel.SNUiState
import com.example.myapplication.viewmodel.SNViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.R


@Composable
fun SNApp() {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),

        ) {it ->
        Surface(
            modifier = Modifier.fillMaxSize()
        ) {


            val snViewModel: SNViewModel =
                viewModel(factory = SNViewModel.Factory)

            when (val state = snViewModel.snUiState) {
                is SNUiState.Success -> {
                    HomeScreen(
                        viewModel = snViewModel,
                        modifier = Modifier.padding(it)
                    )
                }
                else -> {
                    LoginScreen(viewModel = snViewModel, modifier = Modifier.padding(it))
                }
            }

        }
    }
}