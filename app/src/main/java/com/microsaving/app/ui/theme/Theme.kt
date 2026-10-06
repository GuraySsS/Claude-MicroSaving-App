package com.microsaving.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

val Mint = Color(0xFFE8F5E9)
val Leaf = Color(0xFF43A047)
val DeepLeaf = Color(0xFF2E7D32)
val Sunset = Color(0xFFFF7043)
val Overspent = Color(0xFFE53935)
val Ink = Color(0xFF263238)

private val colors = lightColorScheme(
    primary = Leaf,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFC8E6C9),
    onPrimaryContainer = DeepLeaf,
    secondary = Sunset,
    onSecondary = Color.White,
    background = Mint,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    surfaceVariant = Color(0xFFF1F8E9),
    error = Overspent,
)

private val shapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
)

@Composable
fun SummitSaverTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = colors, shapes = shapes, content = content)
}
