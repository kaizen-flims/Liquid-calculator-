package com.kaizenflims.liquidcalculator

import android.app.Application
import com.kaizenflims.liquidcalculator.data.PreferencesRepository
import com.kaizenflims.liquidcalculator.data.liquidCalculatorDataStore
import com.kaizenflims.liquidcalculator.history.HistoryRepository
import com.kaizenflims.liquidcalculator.wallpaper.WallpaperRepository

class LiquidCalculatorApplication : Application() {
    val container: AppContainer by lazy {
        AppContainer(
            preferencesRepository = PreferencesRepository(liquidCalculatorDataStore),
            historyRepository = HistoryRepository(liquidCalculatorDataStore),
            wallpaperRepository = WallpaperRepository(this),
        )
    }
}

data class AppContainer(
    val preferencesRepository: PreferencesRepository,
    val historyRepository: HistoryRepository,
    val wallpaperRepository: WallpaperRepository,
)

