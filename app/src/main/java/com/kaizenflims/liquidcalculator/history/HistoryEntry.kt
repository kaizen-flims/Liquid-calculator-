package com.kaizenflims.liquidcalculator.history

data class HistoryEntry(
    val id: String,
    val expression: String,
    val result: String,
    val timestampMillis: Long,
)

