package com.kaizenflims.liquidcalculator.data

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

val Context.liquidCalculatorDataStore by preferencesDataStore(name = "liquid_calculator_preferences")

