package com.kaizenflims.liquidcalculator.ui

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.weight
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.kaizenflims.liquidcalculator.calculator.CalculatorAction
import com.kaizenflims.liquidcalculator.calculator.CalculatorUiState
import com.kaizenflims.liquidcalculator.calculator.CalculatorViewModel
import com.kaizenflims.liquidcalculator.glass.GlassButtonKind
import com.kaizenflims.liquidcalculator.glass.GlassPalette
import com.kaizenflims.liquidcalculator.glass.LiquidGlassButton
import com.kaizenflims.liquidcalculator.glass.glassPalette
import com.kaizenflims.liquidcalculator.history.HistoryEntry
import com.kaizenflims.liquidcalculator.utils.performCalculatorHaptic
import com.kaizenflims.liquidcalculator.utils.rememberMotionParallax
import com.kaizenflims.liquidcalculator.wallpaper.WallpaperCropper
import com.kaizenflims.liquidcalculator.wallpaper.WallpaperFrame
import com.kaizenflims.liquidcalculator.wallpaper.drawWallpaper

@Composable
fun CalculatorScreen(
    state: CalculatorUiState,
    viewModel: CalculatorViewModel,
) {
    var showHistory by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var rootSize by remember { mutableStateOf(IntSize.Zero) }
    val snackbar = remember { SnackbarHostState() }
    val parallax by rememberMotionParallax(
        enabled = state.preferences.motionEffects && state.pendingWallpaper == null,
    )
    val palette = remember(
        state.wallpaper.asset,
        state.preferences.theme,
        state.preferences.glassIntensity,
    ) {
        glassPalette(
            wallpaper = state.wallpaper.asset,
            theme = state.preferences.theme,
            intensity = state.preferences.glassIntensity,
        )
    }
    val view = LocalView.current
    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) {
            showSettings = false
            viewModel.prepareWallpaper(uri)
        }
    }

    state.message?.let { message ->
        LaunchedEffect(message) {
            snackbar.showSnackbar(message)
            viewModel.consumeMessage()
        }
    }

    BackHandler(enabled = state.pendingWallpaper != null) {
        viewModel.cancelWallpaperCrop()
    }

    if (state.pendingWallpaper != null) {
        val pending = state.pendingWallpaper
        WallpaperCropper(
            asset = pending.asset,
            transform = pending.transform,
            intensity = state.preferences.glassIntensity,
            quality = state.preferences.quality,
            theme = state.preferences.theme,
            haptics = state.preferences.haptics,
            onTransform = viewModel::updatePendingTransform,
            onCancel = viewModel::cancelWallpaperCrop,
            onApply = viewModel::applyPendingWallpaper,
        )
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { rootSize = it },
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawWallpaper(
                frame = state.wallpaper,
                rootSize = rootSize,
                parallax = parallax,
            )
            drawRect(
                Brush.verticalGradient(
                    0f to Color.Black.copy(alpha = 0.12f),
                    0.36f to Color.Transparent,
                    1f to palette.displayScrim,
                ),
            )
        }

        CalculatorLayout(
            state = state,
            wallpaper = state.wallpaper,
            rootSize = rootSize,
            palette = palette,
            parallax = parallax,
            onHistory = { showHistory = true },
            onSettings = { showSettings = true },
            onHaptic = { kind ->
                performCalculatorHaptic(view, kind, state.preferences.haptics)
            },
            onAction = viewModel::dispatch,
        )

        SnackbarHost(
            hostState = snackbar,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .systemBarsPadding()
                .padding(18.dp),
        ) { data ->
            Snackbar(
                snackbarData = data,
                containerColor = Color(0xE61A2430),
                contentColor = Color.White,
            )
        }
    }

    if (showHistory) {
        HistorySheet(
            entries = state.history,
            palette = palette,
            onDismiss = { showHistory = false },
            onClear = viewModel::clearHistory,
            onRecall = { entry: HistoryEntry ->
                viewModel.dispatch(CalculatorAction.Recall(entry.result))
                showHistory = false
            },
        )
    }
    if (showSettings) {
        SettingsSheet(
            preferences = state.preferences,
            hasCustomWallpaper = state.wallpaper.asset.sourceUri != null,
            palette = palette,
            onDismiss = { showSettings = false },
            onChooseWallpaper = {
                picker.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                )
            },
            onRemoveWallpaper = viewModel::removeWallpaper,
            onGlassIntensity = viewModel::setGlassIntensity,
            onMotionEffects = viewModel::setMotionEffects,
            onHaptics = viewModel::setHaptics,
            onQuality = viewModel::setQuality,
            onTheme = viewModel::setTheme,
            onResetAppearance = viewModel::resetAppearance,
        )
    }
}

@Composable
private fun CalculatorLayout(
    state: CalculatorUiState,
    wallpaper: WallpaperFrame,
    rootSize: IntSize,
    palette: GlassPalette,
    parallax: Offset,
    onHistory: () -> Unit,
    onSettings: () -> Unit,
    onHaptic: (GlassButtonKind) -> Unit,
    onAction: (CalculatorAction) -> Unit,
) {
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    if (isLandscape) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Column(
                modifier = Modifier
                    .weight(0.34f)
                    .fillMaxHeight(),
            ) {
                TopControls(
                    wallpaper = wallpaper,
                    rootSize = rootSize,
                    palette = palette,
                    intensity = state.preferences.glassIntensity,
                    quality = state.preferences.quality,
                    parallax = parallax,
                    onHaptic = onHaptic,
                    onHistory = onHistory,
                    onSettings = onSettings,
                )
                DisplayPanel(
                    state = state.calculator,
                    palette = palette,
                    isLandscape = true,
                    onSwipeDown = onHistory,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                )
            }
            CalculatorKeypad(
                isLandscape = true,
                wallpaper = wallpaper,
                rootSize = rootSize,
                palette = palette,
                intensity = state.preferences.glassIntensity,
                quality = state.preferences.quality,
                parallax = parallax,
                onHaptic = onHaptic,
                onAction = onAction,
                modifier = Modifier
                    .weight(0.66f)
                    .fillMaxHeight(),
            )
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(start = 18.dp, end = 18.dp, top = 7.dp, bottom = 14.dp),
        ) {
            TopControls(
                wallpaper = wallpaper,
                rootSize = rootSize,
                palette = palette,
                intensity = state.preferences.glassIntensity,
                quality = state.preferences.quality,
                parallax = parallax,
                onHaptic = onHaptic,
                onHistory = onHistory,
                onSettings = onSettings,
            )
            DisplayPanel(
                state = state.calculator,
                palette = palette,
                isLandscape = false,
                onSwipeDown = onHistory,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.31f),
            )
            Spacer(Modifier.height(11.dp))
            CalculatorKeypad(
                isLandscape = false,
                wallpaper = wallpaper,
                rootSize = rootSize,
                palette = palette,
                intensity = state.preferences.glassIntensity,
                quality = state.preferences.quality,
                parallax = parallax,
                onHaptic = onHaptic,
                onAction = onAction,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.69f),
            )
        }
    }
}

@Composable
private fun TopControls(
    wallpaper: WallpaperFrame,
    rootSize: IntSize,
    palette: GlassPalette,
    intensity: Float,
    quality: com.kaizenflims.liquidcalculator.glass.GlassQuality,
    parallax: Offset,
    onHaptic: (GlassButtonKind) -> Unit,
    onHistory: () -> Unit,
    onSettings: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp),
        horizontalArrangement = Arrangement.End,
    ) {
        LiquidGlassButton(
            label = "↶",
            kind = GlassButtonKind.Utility,
            wallpaper = wallpaper,
            rootSize = rootSize,
            palette = palette,
            intensity = intensity,
            quality = quality,
            parallax = parallax,
            contentDescription = "Calculation history",
            onHaptic = onHaptic,
            onClick = onHistory,
            modifier = Modifier.size(46.dp),
        )
        Spacer(Modifier.size(10.dp))
        LiquidGlassButton(
            label = "•••",
            kind = GlassButtonKind.Utility,
            wallpaper = wallpaper,
            rootSize = rootSize,
            palette = palette,
            intensity = intensity,
            quality = quality,
            parallax = parallax,
            contentDescription = "Appearance settings",
            onHaptic = onHaptic,
            onClick = onSettings,
            modifier = Modifier.size(46.dp),
        )
    }
}
