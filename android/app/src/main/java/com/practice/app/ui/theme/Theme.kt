package com.practice.app.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.practice.app.R

/** Soft charcoal — readable, never harsh. */
val IzadiInk = Color(0xFF2E3C3A)

/** Muted sage-grey for supporting copy. */
val IzadiInkSoft = Color(0xFF6E7F7B)

/** Airy mist background. */
val IzadiMist = Color(0xFFF3F6F5)

/** Gentle sage accent. */
val IzadiSage = Color(0xFF6B958C)

/** Lighter sage for icons and labels. */
val IzadiSageSoft = Color(0xFF87AFA6)

/** Soft white surface for interactive rows. */
val IzadiFoam = Color(0xFFFAFCFB)

/** Soft butter yellow — overdue sessions awaiting wrap-up. */
val IzadiButter = Color(0xFFF3E6C4)

/** Barely-there tint behind icons. */
val IzadiBloom = Color(0xFFE8F0ED)

/** Soft pastel red for destructive actions. */
val IzadiRose = Color(0xFFE8B4B0)

/** Deeper rose for destructive labels. */
val IzadiRoseDeep = Color(0xFF9A5B56)

private val SoftSky = Color(0xFFEAF2F0)
private val SoftCloud = Color(0xFFF7F9F8)

private val OutfitFamily = FontFamily(
    Font(R.font.outfit_extralight, FontWeight.ExtraLight),
    Font(R.font.outfit_light, FontWeight.Light),
    Font(R.font.outfit_bold, FontWeight.Bold),
)

private val ColorScheme = lightColorScheme(
    background = IzadiMist,
    onBackground = IzadiInk,
    surface = IzadiFoam,
    onSurface = IzadiInk,
    primary = IzadiSage,
    onPrimary = Color.White,
    secondary = IzadiBloom,
    onSecondary = IzadiInk,
)

private val AppTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = OutfitFamily,
        fontWeight = FontWeight.Light,
        fontSize = 46.sp,
        letterSpacing = (-0.6).sp,
        lineHeight = 50.sp,
        color = IzadiInk,
    ),
    titleLarge = TextStyle(
        fontFamily = OutfitFamily,
        fontWeight = FontWeight.Light,
        fontSize = 28.sp,
        letterSpacing = (-0.2).sp,
        color = IzadiInk,
    ),
    titleMedium = TextStyle(
        fontFamily = OutfitFamily,
        fontWeight = FontWeight.Light,
        fontSize = 20.sp,
        letterSpacing = 0.1.sp,
        color = IzadiInk,
    ),
    bodyLarge = TextStyle(
        fontFamily = OutfitFamily,
        fontWeight = FontWeight.Light,
        fontSize = 17.sp,
        lineHeight = 25.sp,
        color = IzadiInkSoft,
    ),
    bodyMedium = TextStyle(
        fontFamily = OutfitFamily,
        fontWeight = FontWeight.Light,
        fontSize = 15.sp,
        lineHeight = 21.sp,
        color = IzadiInkSoft,
    ),
    labelLarge = TextStyle(
        fontFamily = OutfitFamily,
        fontWeight = FontWeight.Light,
        fontSize = 13.sp,
        letterSpacing = 2.2.sp,
        color = IzadiSageSoft,
    ),
)

@Composable
fun SoftScreenBackground(
    /** When true, keeps the mint wash through the full height (no fade to white). */
    fullMint: Boolean = false,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(
                if (fullMint) {
                    // Flat mint — no mid-screen light band that reads as a white panel.
                    Modifier.background(SoftSky)
                } else {
                    Modifier.background(
                        Brush.verticalGradient(
                            colors = listOf(SoftSky, IzadiMist, SoftCloud),
                        ),
                    )
                },
            ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0x3387AFA6),
                            Color.Transparent,
                        ),
                        radius = 560f,
                    ),
                )
                .align(Alignment.TopEnd),
        )
        content()
    }
}

@Composable
fun PracticeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ColorScheme,
        typography = AppTypography,
        content = content,
    )
}
