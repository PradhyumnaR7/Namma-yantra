package com.nammayantra.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = Color.White,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = PrimaryDark,
    secondary = Secondary,
    onSecondary = Color.White,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = TextPrimary,
    tertiary = SecondaryLight,
    onTertiary = Color.White,
    background = Background,
    onBackground = TextPrimary,
    surface = Surface,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = Divider,
    error = Error,
    onError = Color.White,
    errorContainer = Color(0xFFFFEDED),
    onErrorContainer = Error,
    inverseSurface = TextPrimary,
    inverseOnSurface = Color.White,
    inversePrimary = PrimaryLight,
    scrim = Color(0x99000000)
)

@Composable
fun NammaYantraTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
