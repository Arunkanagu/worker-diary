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

// Bricolage Grotesque (OFL), static cuts of the variable file: titles and numbers,
// set like the printed dates in a site diary. Inter carries everything you read or tap.
val Bricolage = FontFamily(
    Font(R.font.bricolage_medium, FontWeight.Medium),
    Font(R.font.bricolage_semibold, FontWeight.SemiBold)
)

/** Narrow, large-optical-size cut for the big date and hero numbers. */
val BricolageDisplay = FontFamily(Font(R.font.bricolage_display, FontWeight.SemiBold))

private fun inter(weight: FontWeight, size: Int, line: Int, tracking: Double = 0.0) = TextStyle(
    fontFamily = Inter,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = line.sp,
    letterSpacing = tracking.sp
)

private fun bricolage(weight: FontWeight, size: Int, line: Int, tracking: Double = 0.0) = TextStyle(
    fontFamily = Bricolage,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = line.sp,
    letterSpacing = tracking.sp
)

val Typography = Typography(
    displayLarge = TextStyle(fontFamily = BricolageDisplay, fontWeight = FontWeight.SemiBold, fontSize = 96.sp, lineHeight = 92.sp, letterSpacing = (-2).sp),
    displayMedium = bricolage(FontWeight.SemiBold, 44, 50, -1.0),
    displaySmall = bricolage(FontWeight.SemiBold, 36, 42, -0.75),
    headlineMedium = bricolage(FontWeight.SemiBold, 30, 36, -0.5),
    headlineSmall = bricolage(FontWeight.SemiBold, 24, 30, -0.25),
    titleLarge = bricolage(FontWeight.SemiBold, 21, 28, -0.1),
    titleMedium = inter(FontWeight.SemiBold, 17, 24),
    titleSmall = inter(FontWeight.Medium, 15, 20),
    bodyLarge = inter(FontWeight.Normal, 16, 24),
    bodyMedium = inter(FontWeight.Normal, 14, 20),
    bodySmall = inter(FontWeight.Normal, 13, 18),
    labelLarge = inter(FontWeight.SemiBold, 16, 22),
    labelMedium = inter(FontWeight.Medium, 14, 20),
    labelSmall = inter(FontWeight.Medium, 12, 16, 0.2)
)
