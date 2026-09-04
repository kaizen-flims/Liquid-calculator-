package com.kaizenflims.liquidcalculator.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaizenflims.liquidcalculator.glass.GlassPalette
import com.kaizenflims.liquidcalculator.history.HistoryEntry
import java.text.DateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorySheet(
    entries: List<HistoryEntry>,
    palette: GlassPalette,
    onDismiss: () -> Unit,
    onClear: () -> Unit,
    onRecall: (HistoryEntry) -> Unit,
) {
    val container = if (palette.label.red < 0.5f) {
        Color(0xEEF1F5F8)
    } else {
        Color(0xEE111A26)
    }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = container,
        contentColor = palette.label,
        dragHandle = {
            Surface(
                color = palette.secondaryLabel.copy(alpha = 0.35f),
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 6.dp)
                    .fillMaxWidth(0.1f)
                    .height(4.dp),
            ) {}
        },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = "History",
                    color = palette.label,
                    fontSize = 27.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "Tap a result to use it again",
                    color = palette.secondaryLabel,
                    fontSize = 13.sp,
                )
            }
            if (entries.isNotEmpty()) {
                TextButton(onClick = onClear) {
                    Text("Clear", color = palette.label)
                }
            }
        }
        if (entries.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 56.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "No calculations yet",
                    color = palette.label,
                    fontSize = 18.sp,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Completed calculations will appear here.",
                    color = palette.secondaryLabel,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            val formatter = remember { DateFormat.getTimeInstance(DateFormat.SHORT) }
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 18.dp,
                    end = 18.dp,
                    top = 8.dp,
                    bottom = 30.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                items(entries, key = HistoryEntry::id) { entry ->
                    Surface(
                        color = palette.bodyTint.copy(alpha = 0.13f),
                        shape = RoundedCornerShape(22.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onRecall(entry) },
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                            horizontalAlignment = Alignment.End,
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    text = formatter.format(Date(entry.timestampMillis)),
                                    color = palette.secondaryLabel,
                                    fontSize = 11.sp,
                                )
                                Text(
                                    text = entry.expression,
                                    color = palette.secondaryLabel,
                                    fontSize = 14.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 7.dp),
                                color = palette.rimLight.copy(alpha = 0.12f),
                            )
                            Text(
                                text = entry.result,
                                color = palette.label,
                                fontSize = 27.sp,
                                fontWeight = FontWeight.Light,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        }
    }
}

