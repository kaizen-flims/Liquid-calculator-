package com.kaizenflims.liquidcalculator.history

import java.nio.charset.StandardCharsets
import java.util.Base64

object HistoryCodec {
    private val encoder = Base64.getUrlEncoder().withoutPadding()
    private val decoder = Base64.getUrlDecoder()

    fun encode(entries: List<HistoryEntry>): String = entries.joinToString("\n") { entry ->
        listOf(
            encodeText(entry.id),
            encodeText(entry.expression),
            encodeText(entry.result),
            entry.timestampMillis.toString(),
        ).joinToString("\t")
    }

    fun decode(serialized: String): List<HistoryEntry> {
        if (serialized.isBlank()) return emptyList()
        return serialized.lineSequence().mapNotNull { line ->
            runCatching {
                val fields = line.split('\t')
                require(fields.size == 4)
                HistoryEntry(
                    id = decodeText(fields[0]),
                    expression = decodeText(fields[1]),
                    result = decodeText(fields[2]),
                    timestampMillis = fields[3].toLong(),
                )
            }.getOrNull()
        }.toList()
    }

    private fun encodeText(value: String): String =
        encoder.encodeToString(value.toByteArray(StandardCharsets.UTF_8))

    private fun decodeText(value: String): String =
        String(decoder.decode(value), StandardCharsets.UTF_8)
}

