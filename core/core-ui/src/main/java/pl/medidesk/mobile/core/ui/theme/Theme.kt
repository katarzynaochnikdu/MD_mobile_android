package pl.medidesk.mobile.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = MdBlue,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = MdBlueSurface,
    onPrimaryContainer = MdBlueDark,
    secondary = MdBlueDark,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    tertiary = MdGreen,
    background = MdGrey50,
    surface = androidx.compose.ui.graphics.Color.White,
    onBackground = MdGrey900,
    onSurface = MdGrey900,
    error = MdRed,
    onError = androidx.compose.ui.graphics.Color.White
)

@Composable
fun MdTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = MdTypography,
        content = content
    )
}
