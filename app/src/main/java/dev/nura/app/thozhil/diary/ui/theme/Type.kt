package dev.nura.app.thozhil.diary.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import dev.nura.app.thozhil.diary.R

// Inter, static weights cut from the Google Fonts variable file
// (Light uses the display optical size because it's only used large).
val Inter = FontFamily(
    Font(R.font.inter_light, FontWeight.Light),
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold)
)

private fun inter(weight: FontWeight, size: Int, line: Int, tracking: Double = 0.0) = TextStyle(
    fontFamily = Inter,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = line.sp,
    letterSpacing = tracking.sp
)

val Typography = Typography(
    // Large light headings, like the kit's "Aa" specimen
    displayMedium = inter(FontWeight.Light, 44, 52, -1.0),
    displaySmall = inter(FontWeight.Light, 36, 44, -0.5),
    headlineMedium = inter(FontWeight.Light, 28, 36, -0.25),
    headlineSmall = inter(FontWeight.Medium, 22, 28),
    titleLarge = inter(FontWeight.SemiBold, 20, 28),
    titleMedium = inter(FontWeight.SemiBold, 17, 24),
    titleSmall = inter(FontWeight.Medium, 15, 20),
    bodyLarge = inter(FontWeight.Normal, 16, 24),
    bodyMedium = inter(FontWeight.Normal, 14, 20),
    bodySmall = inter(FontWeight.Normal, 13, 18),
    labelLarge = inter(FontWeight.Medium, 16, 22),
    labelMedium = inter(FontWeight.Medium, 14, 20),
    labelSmall = inter(FontWeight.Medium, 12, 16, 0.2)
)
