package com.kaizenflims.liquidcalculator.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.weight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.kaizenflims.liquidcalculator.calculator.BinaryOperator
import com.kaizenflims.liquidcalculator.calculator.CalculatorAction
import com.kaizenflims.liquidcalculator.calculator.CalculatorConstant
import com.kaizenflims.liquidcalculator.calculator.UnaryOperation
import com.kaizenflims.liquidcalculator.glass.GlassButtonKind
import com.kaizenflims.liquidcalculator.glass.GlassPalette
import com.kaizenflims.liquidcalculator.glass.GlassQuality
import com.kaizenflims.liquidcalculator.glass.LiquidGlassButton
import com.kaizenflims.liquidcalculator.wallpaper.WallpaperFrame

private data class KeySpec(
    val label: String,
    val description: String,
    val kind: GlassButtonKind,
    val action: CalculatorAction,
)

@Composable
fun CalculatorKeypad(
    isLandscape: Boolean,
    wallpaper: WallpaperFrame,
    rootSize: IntSize,
    palette: GlassPalette,
    intensity: Float,
    quality: GlassQuality,
    parallax: Offset,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onHaptic: (GlassButtonKind) -> Unit,
    onAction: (CalculatorAction) -> Unit,
) {
    if (isLandscape) {
        LandscapeKeypad(
            wallpaper,
            rootSize,
            palette,
            intensity,
            quality,
            parallax,
            modifier,
            enabled,
            onHaptic,
            onAction,
        )
    } else {
        PortraitKeypad(
            wallpaper,
            rootSize,
            palette,
            intensity,
            quality,
            parallax,
            modifier,
            enabled,
            onHaptic,
            onAction,
        )
    }
}

@Composable
private fun PortraitKeypad(
    wallpaper: WallpaperFrame,
    rootSize: IntSize,
    palette: GlassPalette,
    intensity: Float,
    quality: GlassQuality,
    parallax: Offset,
    modifier: Modifier,
    enabled: Boolean,
    onHaptic: (GlassButtonKind) -> Unit,
    onAction: (CalculatorAction) -> Unit,
) {
    val rows = remember {
        listOf(
            listOf(
                key("AC", "All clear", GlassButtonKind.Utility, CalculatorAction.Clear),
                key("±", "Toggle sign", GlassButtonKind.Utility, CalculatorAction.ToggleSign),
                key("%", "Percent", GlassButtonKind.Utility, CalculatorAction.Percent),
                operator("÷", "Divide", BinaryOperator.Divide),
            ),
            listOf(
                digit(7), digit(8), digit(9), operator("×", "Multiply", BinaryOperator.Multiply),
            ),
            listOf(
                digit(4), digit(5), digit(6), operator("−", "Subtract", BinaryOperator.Subtract),
            ),
            listOf(
                digit(1), digit(2), digit(3), operator("+", "Add", BinaryOperator.Add),
            ),
        )
    }
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(11.dp),
    ) {
        rows.forEachIndexed { index, row ->
            AnimatedKeyRow(
                keys = row,
                rowIndex = index,
                wallpaper = wallpaper,
                rootSize = rootSize,
                palette = palette,
                intensity = intensity,
                quality = quality,
                parallax = parallax,
                enabled = enabled,
                onHaptic = onHaptic,
                onAction = onAction,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            )
        }
        AnimatedFinalRow(
            rowIndex = 4,
            wallpaper = wallpaper,
            rootSize = rootSize,
            palette = palette,
            intensity = intensity,
            quality = quality,
            parallax = parallax,
            enabled = enabled,
            onHaptic = onHaptic,
            onAction = onAction,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        )
    }
}

@Composable
private fun AnimatedFinalRow(
    rowIndex: Int,
    wallpaper: WallpaperFrame,
    rootSize: IntSize,
    palette: GlassPalette,
    intensity: Float,
    quality: GlassQuality,
    parallax: Offset,
    enabled: Boolean,
    onHaptic: (GlassButtonKind) -> Unit,
    onAction: (CalculatorAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val animationModifier = keyRowEntrance(rowIndex)
    Row(
        modifier = modifier.then(animationModifier),
        horizontalArrangement = Arrangement.spacedBy(11.dp),
    ) {
        CalculatorKey(
            spec = digit(0),
            wallpaper = wallpaper,
            rootSize = rootSize,
            palette = palette,
            intensity = intensity,
            quality = quality,
            parallax = parallax,
            enabled = enabled,
            onHaptic = onHaptic,
            onAction = onAction,
            modifier = Modifier
                .weight(2f)
                .fillMaxHeight(),
        )
        CalculatorKey(
            spec = key(".", "Decimal point", GlassButtonKind.Number, CalculatorAction.Decimal),
            wallpaper = wallpaper,
            rootSize = rootSize,
            palette = palette,
            intensity = intensity,
            quality = quality,
            parallax = parallax,
            enabled = enabled,
            onHaptic = onHaptic,
            onAction = onAction,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
        )
        CalculatorKey(
            spec = key("=", "Equals", GlassButtonKind.Equals, CalculatorAction.Equals),
            wallpaper = wallpaper,
            rootSize = rootSize,
            palette = palette,
            intensity = intensity,
            quality = quality,
            parallax = parallax,
            enabled = enabled,
            onHaptic = onHaptic,
            onAction = onAction,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
        )
    }
}

@Composable
private fun LandscapeKeypad(
    wallpaper: WallpaperFrame,
    rootSize: IntSize,
    palette: GlassPalette,
    intensity: Float,
    quality: GlassQuality,
    parallax: Offset,
    modifier: Modifier,
    enabled: Boolean,
    onHaptic: (GlassButtonKind) -> Unit,
    onAction: (CalculatorAction) -> Unit,
) {
    val scientific = GlassButtonKind.Scientific
    val rows = remember {
        listOf(
            listOf(
                unary("sin", "Sine in degrees", UnaryOperation.Sine),
                unary("cos", "Cosine in degrees", UnaryOperation.Cosine),
                unary("tan", "Tangent in degrees", UnaryOperation.Tangent),
                key("AC", "All clear", GlassButtonKind.Utility, CalculatorAction.Clear),
                key("±", "Toggle sign", GlassButtonKind.Utility, CalculatorAction.ToggleSign),
                key("%", "Percent", GlassButtonKind.Utility, CalculatorAction.Percent),
            ),
            listOf(
                unary("ln", "Natural logarithm", UnaryOperation.NaturalLog),
                unary("log", "Base ten logarithm", UnaryOperation.CommonLog),
                unary("√", "Square root", UnaryOperation.SquareRoot),
                digit(7), digit(8), digit(9),
            ),
            listOf(
                unary("x²", "Square", UnaryOperation.Square),
                unary("1/x", "Reciprocal", UnaryOperation.Reciprocal),
                unary("x!", "Factorial", UnaryOperation.Factorial),
                digit(4), digit(5), digit(6),
            ),
            listOf(
                key("π", "Pi", scientific, CalculatorAction.Constant(CalculatorConstant.Pi)),
                key("e", "Euler's number", scientific, CalculatorAction.Constant(CalculatorConstant.Euler)),
                operator("÷", "Divide", BinaryOperator.Divide),
                digit(1), digit(2), digit(3),
            ),
            listOf(
                operator("×", "Multiply", BinaryOperator.Multiply),
                operator("−", "Subtract", BinaryOperator.Subtract),
                operator("+", "Add", BinaryOperator.Add),
                digit(0),
                key(".", "Decimal point", GlassButtonKind.Number, CalculatorAction.Decimal),
                key("=", "Equals", GlassButtonKind.Equals, CalculatorAction.Equals),
            ),
        )
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        rows.forEachIndexed { index, row ->
            AnimatedKeyRow(
                keys = row,
                rowIndex = index,
                wallpaper = wallpaper,
                rootSize = rootSize,
                palette = palette,
                intensity = intensity,
                quality = quality,
                parallax = parallax,
                enabled = enabled,
                onHaptic = onHaptic,
                onAction = onAction,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                spacing = 8,
            )
        }
    }
}

@Composable
private fun AnimatedKeyRow(
    keys: List<KeySpec>,
    rowIndex: Int,
    wallpaper: WallpaperFrame,
    rootSize: IntSize,
    palette: GlassPalette,
    intensity: Float,
    quality: GlassQuality,
    parallax: Offset,
    enabled: Boolean,
    onHaptic: (GlassButtonKind) -> Unit,
    onAction: (CalculatorAction) -> Unit,
    modifier: Modifier = Modifier,
    spacing: Int = 11,
) {
    Row(
        modifier = modifier.then(keyRowEntrance(rowIndex)),
        horizontalArrangement = Arrangement.spacedBy(spacing.dp),
    ) {
        keys.forEach { spec ->
            CalculatorKey(
                spec = spec,
                wallpaper = wallpaper,
                rootSize = rootSize,
                palette = palette,
                intensity = intensity,
                quality = quality,
                parallax = parallax,
                enabled = enabled,
                onHaptic = onHaptic,
                onAction = onAction,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
        }
    }
}

@Composable
private fun keyRowEntrance(rowIndex: Int): Modifier {
    var entered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { entered = true }
    val progress by animateFloatAsState(
        targetValue = if (entered) 1f else 0f,
        animationSpec = tween(
            durationMillis = 210,
            delayMillis = 28 + rowIndex * 18,
        ),
        label = "key row entrance",
    )
    return Modifier.graphicsLayer {
        alpha = 0.45f + progress * 0.55f
        translationY = (1f - progress) * 13f
    }
}

@Composable
private fun CalculatorKey(
    spec: KeySpec,
    wallpaper: WallpaperFrame,
    rootSize: IntSize,
    palette: GlassPalette,
    intensity: Float,
    quality: GlassQuality,
    parallax: Offset,
    enabled: Boolean,
    onHaptic: (GlassButtonKind) -> Unit,
    onAction: (CalculatorAction) -> Unit,
    modifier: Modifier,
) {
    LiquidGlassButton(
        label = spec.label,
        kind = spec.kind,
        wallpaper = wallpaper,
        rootSize = rootSize,
        palette = palette,
        intensity = intensity,
        quality = quality,
        parallax = parallax,
        enabled = enabled,
        contentDescription = spec.description,
        onHaptic = onHaptic,
        onClick = { onAction(spec.action) },
        modifier = modifier,
    )
}

private fun digit(value: Int) = key(
    label = value.toString(),
    description = value.toString(),
    kind = GlassButtonKind.Number,
    action = CalculatorAction.Digit(value),
)

private fun operator(
    label: String,
    description: String,
    value: BinaryOperator,
) = key(
    label = label,
    description = description,
    kind = GlassButtonKind.Operator,
    action = CalculatorAction.Operator(value),
)

private fun unary(
    label: String,
    description: String,
    value: UnaryOperation,
) = key(
    label = label,
    description = description,
    kind = GlassButtonKind.Scientific,
    action = CalculatorAction.Unary(value),
)

private fun key(
    label: String,
    description: String,
    kind: GlassButtonKind,
    action: CalculatorAction,
) = KeySpec(label, description, kind, action)

