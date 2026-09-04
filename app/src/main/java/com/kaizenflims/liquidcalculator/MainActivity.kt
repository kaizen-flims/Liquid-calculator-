package com.kaizenflims.liquidcalculator

import android.os.Bundle
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kaizenflims.liquidcalculator.calculator.CalculatorViewModel
import com.kaizenflims.liquidcalculator.ui.CalculatorScreen
import com.kaizenflims.liquidcalculator.ui.theme.LiquidCalculatorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        setContent {
            LiquidCalculatorTheme {
                val application = application as LiquidCalculatorApplication
                val viewModel: CalculatorViewModel = viewModel(
                    factory = CalculatorViewModel.Factory(application.container),
                )
                val state by viewModel.uiState.collectAsStateWithLifecycle()
                CalculatorScreen(
                    state = state,
                    viewModel = viewModel,
                )
            }
        }
    }
}
