package com.tapiceria.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Paleta principal: verde natural y tonos cálidos.
private val VerdePrincipal = Color(0xFF52745A)
private val VerdeOscuro = Color(0xFF29352D)
private val VerdeSuave = Color(0xFFDCE7D9)
private val FondoMarfil = Color(0xFFF7F4ED)
private val BlancoSuperficie = Color(0xFFFFFEFA)
private val TextoPrincipal = Color(0xFF282D29)
private val TextoSecundario = Color(0xFF626A63)
private val RojoError = Color(0xFFBA3939)

// Colores utilizados cuando el dispositivo tiene activado el modo oscuro.
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFA7C9A8),
    onPrimary = Color(0xFF183821),
    secondary = Color(0xFFD1BFA0),
    onSecondary = Color(0xFF342B1B),
    tertiary = Color(0xFFE2BBA0),
    background = Color(0xFF191D19),
    onBackground = Color(0xFFE4E7E0),
    surface = Color(0xFF222822),
    onSurface = Color(0xFFE4E7E0),
    surfaceVariant = Color(0xFF303930),
    onSurfaceVariant = Color(0xFFC0C8BE),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

// Colores utilizados por defecto en la aplicación.
private val LightColorScheme = lightColorScheme(
    primary = VerdePrincipal,
    onPrimary = Color.White,
    primaryContainer = VerdeSuave,
    onPrimaryContainer = VerdeOscuro,

    secondary = Color(0xFF8B7150),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEDE1CE),
    onSecondaryContainer = Color(0xFF382D1E),

    tertiary = Color(0xFF647C6B),
    onTertiary = Color.White,

    background = FondoMarfil,
    onBackground = TextoPrincipal,

    surface = BlancoSuperficie,
    onSurface = TextoPrincipal,

    surfaceVariant = Color(0xFFECEFE8),
    onSurfaceVariant = TextoSecundario,

    error = RojoError,
    onError = Color.White
)

/**
 * Tema visual de TapiceriaApp.
 *
 * Los colores se mantienen controlados por la aplicación para que
 * la interfaz sea consistente en distintos dispositivos Android.
 */
@Composable
fun TapiceriaDamianTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Se conserva la opción por compatibilidad, pero se desactiva por defecto.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // Los colores dinámicos solo se utilizan si se solicitan explícitamente.
    val colorScheme = when {
        dynamicColor && darkTheme -> DarkColorScheme
        dynamicColor -> LightColorScheme
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}