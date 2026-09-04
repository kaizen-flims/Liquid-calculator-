package com.kaizenflims.liquidcalculator.calculator

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kaizenflims.liquidcalculator.AppContainer
import com.kaizenflims.liquidcalculator.data.AppPreferences
import com.kaizenflims.liquidcalculator.data.PreferencesRepository
import com.kaizenflims.liquidcalculator.glass.GlassQuality
import com.kaizenflims.liquidcalculator.glass.GlassTheme
import com.kaizenflims.liquidcalculator.history.HistoryEntry
import com.kaizenflims.liquidcalculator.history.HistoryRepository
import com.kaizenflims.liquidcalculator.wallpaper.WallpaperAsset
import com.kaizenflims.liquidcalculator.wallpaper.WallpaperFrame
import com.kaizenflims.liquidcalculator.wallpaper.WallpaperRepository
import com.kaizenflims.liquidcalculator.wallpaper.WallpaperTransform
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PendingWallpaper(
    val asset: WallpaperAsset,
    val transform: WallpaperTransform = WallpaperTransform(),
)

data class CalculatorUiState(
    val calculator: CalculatorState = CalculatorState(),
    val preferences: AppPreferences = AppPreferences(),
    val history: List<HistoryEntry> = emptyList(),
    val wallpaper: WallpaperFrame,
    val pendingWallpaper: PendingWallpaper? = null,
    val message: String? = null,
)

private data class CoreUiState(
    val calculator: CalculatorState,
    val preferences: AppPreferences,
    val history: List<HistoryEntry>,
    val wallpaperAsset: WallpaperAsset,
)

class CalculatorViewModel(
    private val preferencesRepository: PreferencesRepository,
    private val historyRepository: HistoryRepository,
    private val wallpaperRepository: WallpaperRepository,
    private val now: () -> Long = System::currentTimeMillis,
) : ViewModel() {
    private val engine = CalculatorEngine()
    private val calculator = MutableStateFlow(CalculatorState())
    private val wallpaperAsset = MutableStateFlow(wallpaperRepository.defaultWallpaper)
    private val pendingWallpaper = MutableStateFlow<PendingWallpaper?>(null)
    private val message = MutableStateFlow<String?>(null)

    private val preferences = preferencesRepository.preferences.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = AppPreferences(),
    )
    private val history = historyRepository.history.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList(),
    )

    private val coreUiState = combine(
        calculator,
        preferences,
        history,
        wallpaperAsset,
    ) { calculatorState, preferenceState, historyState, asset ->
        CoreUiState(
            calculator = calculatorState,
            preferences = preferenceState,
            history = historyState,
            wallpaperAsset = asset,
        )
    }

    val uiState: StateFlow<CalculatorUiState> = combine(
        coreUiState,
        pendingWallpaper,
        message,
    ) { core, pending, currentMessage ->
        CalculatorUiState(
            calculator = core.calculator,
            preferences = core.preferences,
            history = core.history,
            wallpaper = WallpaperFrame(
                asset = core.wallpaperAsset,
                transform = WallpaperTransform(
                    zoom = core.preferences.wallpaperZoom,
                    offsetX = core.preferences.wallpaperOffsetX,
                    offsetY = core.preferences.wallpaperOffsetY,
                ),
            ),
            pendingWallpaper = pending,
            message = currentMessage,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CalculatorUiState(
            wallpaper = WallpaperFrame(
                wallpaperRepository.defaultWallpaper,
                WallpaperTransform(),
            ),
        ),
    )

    init {
        viewModelScope.launch {
            preferences
                .map { it.wallpaperUri }
                .distinctUntilChanged()
                .collect { uri ->
                    wallpaperAsset.value = if (uri == null) {
                        wallpaperRepository.defaultWallpaper
                    } else {
                        wallpaperRepository.load(uri) ?: run {
                            message.value = "That wallpaper could not be opened. Restored the default."
                            preferencesRepository.clearWallpaper()
                            wallpaperRepository.defaultWallpaper
                        }
                    }
                }
        }
    }

    fun dispatch(action: CalculatorAction) {
        val update = engine.reduce(calculator.value, action)
        calculator.value = update.state
        update.completedCalculation?.let { completed ->
            viewModelScope.launch {
                historyRepository.add(
                    expression = completed.expression,
                    result = completed.result,
                    timestampMillis = now(),
                )
            }
        }
    }

    fun prepareWallpaper(uri: Uri) {
        wallpaperRepository.persistReadPermission(uri)
        viewModelScope.launch {
            val asset = wallpaperRepository.load(uri.toString())
            if (asset == null) {
                message.value = "That image could not be opened."
            } else {
                pendingWallpaper.value = PendingWallpaper(asset)
            }
        }
    }

    fun updatePendingTransform(transform: WallpaperTransform) {
        pendingWallpaper.update { it?.copy(transform = transform.sanitized()) }
    }

    fun applyPendingWallpaper() {
        val pending = pendingWallpaper.value ?: return
        wallpaperAsset.value = pending.asset
        pendingWallpaper.value = null
        viewModelScope.launch {
            preferencesRepository.saveWallpaper(
                uri = requireNotNull(pending.asset.sourceUri),
                zoom = pending.transform.zoom,
                offsetX = pending.transform.offsetX,
                offsetY = pending.transform.offsetY,
            )
        }
    }

    fun cancelWallpaperCrop() {
        pendingWallpaper.value = null
    }

    fun removeWallpaper() {
        wallpaperAsset.value = wallpaperRepository.defaultWallpaper
        viewModelScope.launch { preferencesRepository.clearWallpaper() }
    }

    fun clearHistory() {
        viewModelScope.launch { historyRepository.clear() }
    }

    fun setGlassIntensity(value: Float) {
        viewModelScope.launch { preferencesRepository.setGlassIntensity(value) }
    }

    fun setMotionEffects(enabled: Boolean) {
        viewModelScope.launch { preferencesRepository.setMotionEffects(enabled) }
    }

    fun setHaptics(enabled: Boolean) {
        viewModelScope.launch { preferencesRepository.setHaptics(enabled) }
    }

    fun setQuality(quality: GlassQuality) {
        viewModelScope.launch { preferencesRepository.setQuality(quality) }
    }

    fun setTheme(theme: GlassTheme) {
        viewModelScope.launch { preferencesRepository.setTheme(theme) }
    }

    fun resetAppearance() {
        wallpaperAsset.value = wallpaperRepository.defaultWallpaper
        viewModelScope.launch { preferencesRepository.resetAppearance() }
    }

    fun consumeMessage() {
        message.value = null
    }

    class Factory(
        private val container: AppContainer,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CalculatorViewModel(
                preferencesRepository = container.preferencesRepository,
                historyRepository = container.historyRepository,
                wallpaperRepository = container.wallpaperRepository,
            ) as T
        }
    }
}
