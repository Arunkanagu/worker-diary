package dev.nura.app.thozhil.diary.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Calm Kit is a light design, so the app uses one light scheme.
private val CalmColorScheme = lightColorScheme(
    primary = SageDeep,
    onPrimary = Color.White,
    primaryContainer = Mist,
    onPrimaryContainer = SageInk,
    secondary = Sage,
    onSecondary = Color.White,
    secondaryContainer = Moss,
    onSecondaryContainer = SageInk,
    tertiary = Sand,
    onTertiary = Ink,
    background = Ivory,
    onBackground = Ink,
    surface = Ivory,
    onSurface = Ink,
    surfaceVariant = SandLight,
    onSurfaceVariant = InkMuted,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = IvoryCard,
    surfaceContainer = IvoryCard,
    surfaceContainerHigh = IvoryCard,
    surfaceContainerHighest = SandLight,
    outline = Line,
    outlineVariant = SandLight,
    error = ErrorRed,
    onError = Color.White
)

val CalmShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun ThozhildiaryTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CalmColorScheme,
        typography = Typography,
        shapes = CalmShapes,
        content = content
    )
}
