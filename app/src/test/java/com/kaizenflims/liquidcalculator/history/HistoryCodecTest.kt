package com.kaizenflims.liquidcalculator.history

import org.junit.Assert.assertEquals
import org.junit.Test

class HistoryCodecTest {
    @Test
    fun roundTripPreservesUnicodeAndDelimiters() {
        val entries = listOf(
            HistoryEntry(
                id = "first",
                expression = "9 ÷ 3\nthen",
                result = "3\t",
                timestampMillis = 123456789L,
            ),
        )

        assertEquals(entries, HistoryCodec.decode(HistoryCodec.encode(entries)))
    }

    @Test
    fun malformedRowsDoNotDestroyValidHistory() {
        val entry = HistoryEntry("id", "1 + 1", "2", 42L)
        val serialized = HistoryCodec.encode(listOf(entry)) + "\nnot-valid"

        assertEquals(listOf(entry), HistoryCodec.decode(serialized))
    }
}

