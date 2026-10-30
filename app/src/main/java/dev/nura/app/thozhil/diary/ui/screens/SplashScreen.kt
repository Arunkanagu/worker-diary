package dev.nura.app.thozhil.diary.ui.screens

import android.app.Activity
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import dev.nura.app.thozhil.diary.R
import dev.nura.app.thozhil.diary.ui.components.AppLogo
import dev.nura.app.thozhil.diary.ui.theme.Amber
import dev.nura.app.thozhil.diary.ui.theme.Cover
import dev.nura.app.thozhil.diary.ui.theme.OnCoverMuted
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** A bookmark ribbon with a V-notch at the bottom. */
private val RibbonShape = GenericShape { size, _ ->
    moveTo(0f, 0f)
    lineTo(size.width, 0f)
    lineTo(size.width, size.height)
    lineTo(size.width / 2f, size.height - size.width * 0.55f)
    lineTo(0f, size.height)
    close()
}

private val LogoSize = 112.dp
private val RibbonDrop = 54.dp

/**
 * The diary cover opens: the logo settles in, the amber bookmark drops out beneath it and
 * sways, the name tightens into place and the page rules draw underneath. Compose follows
 * the system animator scale, so with animations off everything simply appears.
 */
@Composable
fun SplashScreen(
    /** Asked once the opening animation ends; reads the database, so it suspends. */
    isLoggedIn: suspend () -> Boolean,
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    val logo = remember { Animatable(0f) }
    val ribbon = remember { Animatable(0f) }
    val sway = remember { Animatable(0f) }
    val title = remember { Animatable(0f) }
    val rules = remember { Animatable(0f) }
    val leave = remember { Animatable(0f) }

    // Light status bar icons on the dark cover, back to dark when the paper screens take over
    val view = LocalView.current
    DisposableEffect(Unit) {
        val window = (view.context as? Activity)?.window
        val controller = window?.let { WindowCompat.getInsetsController(it, view) }
        controller?.isAppearanceLightStatusBars = false
        controller?.isAppearanceLightNavigationBars = false
        onDispose {
            controller?.isAppearanceLightStatusBars = true
            controller?.isAppearanceLightNavigationBars = true
        }
    }

    LaunchedEffect(Unit) {
        launch { logo.animateTo(1f, spring(dampingRatio = 0.62f, stiffness = Spring.StiffnessLow)) }
        launch {
            delay(260)
            ribbon.animateTo(1f, tween(420, easing = FastOutSlowInEasing))
            // A small swing as the ribbon comes to rest
            sway.animateTo(1f, spring(dampingRatio = 0.28f, stiffness = 120f))
        }
        launch {
            delay(420)
            title.animateTo(1f, tween(620, easing = FastOutSlowInEasing))
        }
        delay(700)
        rules.animateTo(1f, tween(560, easing = FastOutSlowInEasing))
        delay(380)
        leave.animateTo(1f, tween(220))
        if (isLoggedIn()) onNavigateToHome() else onNavigateToLogin()
    }

    val appTitle = stringResource(R.string.thozhil_diary)
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Cover)
            .semantics { contentDescription = appTitle }
            .graphicsLayer { alpha = 1f - leave.value }
    ) {
        // Everything hangs off the logo's centre, a little above the middle of the screen
        val logoTop = maxHeight * 0.40f - LogoSize / 2

        // The ribbon sits behind the book and drops out from under it
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(x = 30.dp, y = logoTop + LogoSize * 0.5f)
                .width(20.dp)
                .height(LogoSize * 0.5f + RibbonDrop * ribbon.value)
                .graphicsLayer {
                    transformOrigin = TransformOrigin(0.5f, 0f)
                    rotationZ = (1f - sway.value) * 9f * ribbon.value
                    // Hidden until the book has arrived, so it never shows on its own
                    alpha = ribbon.value
                }
                .background(Amber, RibbonShape)
        )

        AppLogo(
            size = LogoSize,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = logoTop)
                .graphicsLayer {
                    val s = 0.72f + 0.28f * logo.value
                    scaleX = s
                    scaleY = s
                    alpha = logo.value.coerceIn(0f, 1f)
                }
        )

        Text(
            text = appTitle,
            style = MaterialTheme.typography.displaySmall.copy(
                letterSpacing = (6f - 6.75f * title.value).sp
            ),
            color = Color.White,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = logoTop + LogoSize + RibbonDrop + 28.dp)
                .graphicsLayer {
                    alpha = title.value
                    translationY = (1f - title.value) * 14.dp.toPx()
                }
        )

        // Two page rules drawing across, then the tagline written on them
        Canvas(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = logoTop + LogoSize + RibbonDrop + 92.dp)
                .size(width = 196.dp, height = 40.dp)
        ) {
            val stroke = 1.dp.toPx()
            listOf(0f, size.height).forEach { y ->
                drawLine(
                    color = OnCoverMuted.copy(alpha = 0.35f),
                    start = Offset(0f, y),
                    end = Offset(size.width * rules.value, y),
                    strokeWidth = stroke
                )
            }
        }
        Text(
            text = stringResource(R.string.your_daily_work_diary),
            style = MaterialTheme.typography.bodyLarge,
            color = OnCoverMuted,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = logoTop + LogoSize + RibbonDrop + 100.dp)
                .graphicsLayer { alpha = ((rules.value - 0.4f) / 0.6f).coerceIn(0f, 1f) }
        )
    }
}
