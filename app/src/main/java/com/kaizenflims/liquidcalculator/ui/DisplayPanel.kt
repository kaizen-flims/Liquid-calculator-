package com.kaizenflims.liquidcalculator.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import com.kaizenflims.liquidcalculator.calculator.CalculatorState
import com.kaizenflims.liquidcalculator.glass.GlassPalette

@Composable
fun DisplayPanel(
    state: CalculatorState,
    palette: GlassPalette,
    isLandscape: Boolean,
    modifier: Modifier = Modifier,
    onSwipeDown: () -> Unit,
) {
    val threshold = with(LocalDensity.current) { 58f * density }
    var dragDistance = remember { 0f }
    Column(
        modifier = modifier
            .pointerInput(onSwipeDown) {
                detectVerticalDragGestures(
                    onDragStart = { dragDistance = 0f },
                    onVerticalDrag = { change, amount ->
                        if (amount > 0f) dragDistance += amount
                        change.consume()
                    },
                    onDragEnd = {
                        if (dragDistance >= threshold) onSwipeDown()
                        dragDistance = 0f
                    },
                    onDragCancel = { dragDistance = 0f },
                )
            }
            .semantics {
                contentDescription = buildString {
                    if (state.expression.isNotBlank()) {
                        append(state.expression)
                        append(". ")
                    }
                    append("Result ")
                    append(state.display)
                    append(". Swipe down for history.")
                }
            }
            .padding(horizontal = if (isLandscape) 14.dp else 4.dp),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.End,
    ) {
        Text(
            text = if (state.expression.isBlank() && isLandscape) "DEG" else state.expression,
            color = palette.secondaryLabel,
            fontSize = if (isLandscape) 15.sp else 18.sp,
            lineHeight = if (isLandscape) 19.sp else 23.sp,
            textAlign = TextAlign.End,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(bottom = if (isLandscape) 5.dp else 8.dp),
        )
        AnimatedContent(
            targetState = state.display,
            transitionSpec = {
                (fadeIn(tween(90)) + slideInVertically(tween(110)) { it / 9 })
                    .togetherWith(
                        fadeOut(tween(65)) + slideOutVertically(tween(90)) { -it / 9 },
                    )
            },
            label = "result transition",
        ) { value ->
            Text(
                text = value,
                color = palette.label,
                fontSize = resultFontSize(value, isLandscape).sp,
                lineHeight = (resultFontSize(value, isLandscape) * 1.05f).sp,
                fontWeight = FontWeight.Light,
                letterSpacing = (-1.2).sp,
                textAlign = TextAlign.End,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Clip,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

private fun resultFontSize(value: String, landscape: Boolean): Float {
    val count = value.length
    return if (landscape) {
        when {
            count <= 8 -> 54f
            count <= 12 -> 44f
            count <= 17 -> 34f
            else -> 27f
        }
    } else {
        when {
            count <= 7 -> 72f
            count <= 10 -> 61f
            count <= 14 -> 49f
            count <= 19 -> 39f
            else -> 30f
        }
    }
}
