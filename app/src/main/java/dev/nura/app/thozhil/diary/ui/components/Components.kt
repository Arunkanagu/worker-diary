package dev.nura.app.thozhil.diary.ui.components

import androidx.compose.ui.res.stringResource
import androidx.core.view.WindowCompat
import androidx.compose.ui.platform.LocalView
import androidx.compose.runtime.DisposableEffect
import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import dev.nura.app.thozhil.diary.util.loadProfilePhoto
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.nura.app.thozhil.diary.R
import dev.nura.app.thozhil.diary.ui.theme.Ink
import dev.nura.app.thozhil.diary.ui.theme.InkMuted
import dev.nura.app.thozhil.diary.ui.theme.IvoryCard
import dev.nura.app.thozhil.diary.ui.theme.Mist
import dev.nura.app.thozhil.diary.ui.theme.Sage
import dev.nura.app.thozhil.diary.ui.theme.SageInk
import dev.nura.app.thozhil.diary.ui.theme.SandLight

/** The app icon (ic_thozhil_diary_logo) as a soft, rounded tile. */
@Composable
fun AppLogo(size: Dp, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(size * 0.28f)
    Box(
        modifier = modifier
            .size(size)
            .shadow(
                elevation = 10.dp,
                shape = shape,
                ambientColor = SheetShadow.copy(alpha = 0.12f),
                spotColor = SheetShadow.copy(alpha = 0.22f)
            )
            .clip(shape)
    ) {
        Image(
            painter = painterResource(R.drawable.app_logo),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            // The artwork has wide ivory padding; zoom so the diary fills the tile
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = 1.4f
                    scaleY = 1.4f
                }
        )
    }
}

/** A diary sheet: white paper with a hairline rule and a short, crisp green-tinted shadow. */
@Composable
fun SoftCard(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.large,
    color: Color = IvoryCard,
    /** Hairline rule around the sheet; turn off on dark fills. */
    highlightEdge: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .shadow(
                elevation = 4.dp,
                shape = shape,
                ambientColor = SheetShadow.copy(alpha = 0.10f),
                spotColor = SheetShadow.copy(alpha = 0.14f)
            )
            .clip(shape)
            .background(color)
            .then(
                if (highlightEdge) Modifier.border(BorderStroke(1.dp, SandLight), shape)
                else Modifier
            ),
        content = content
    )
}

/** Shadows lean green so they sit in the palette instead of greying it. */
private val SheetShadow = Color(0xFF173A2B)

/** Initials avatar with an "active" dot, like the kit's profile card. */
@Composable
fun InitialsAvatar(name: String, size: Dp, modifier: Modifier = Modifier, showDot: Boolean = true) {
    val initials = name.trim()
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
        .ifEmpty { "?" }
    Box(modifier = modifier.size(size)) {
        Box(
            modifier = Modifier
                .size(size)
                .background(Mist, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                color = SageInk,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = (size.value * 0.36f).sp)
            )
        }
        if (showDot) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(size * 0.24f)
                    .background(Sage, CircleShape)
                    .border(2.dp, IvoryCard, CircleShape)
            )
        }
    }
}

/** The profile photo when there is one, otherwise [InitialsAvatar]. */
@Composable
fun ProfileAvatar(name: String, photoPath: String, size: Dp, modifier: Modifier = Modifier, showDot: Boolean = true) {
    val photo by produceState<ImageBitmap?>(null, photoPath) {
        value = loadProfilePhoto(photoPath)?.asImageBitmap()
    }
    val image = photo
    if (image == null) {
        InitialsAvatar(name = name, size = size, modifier = modifier, showDot = showDot)
        return
    }
    Box(modifier = modifier.size(size)) {
        Image(
            bitmap = image,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .border(1.dp, SandLight, CircleShape)
        )
        if (showDot) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(size * 0.24f)
                    .background(Sage, CircleShape)
                    .border(2.dp, IvoryCard, CircleShape)
            )
        }
    }
}

/** Rounded sage progress bar on a sand track. */
@Composable
fun CalmProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    height: Dp = 12.dp,
    trackColor: Color = SandLight,
    fillColor: Color = Sage
) {
    val clamped = progress.coerceIn(0f, 1f)
    Box(
        modifier = modifier
            .height(height)
            .clip(CircleShape)
            .background(trackColor)
            .semantics { progressBarRangeInfo = ProgressBarRangeInfo(clamped, 0f..1f) }
    ) {
        if (clamped > 0f) {
            Box(
                Modifier
                    .fillMaxWidth(clamped)
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .background(fillColor)
            )
        }
    }
}

/** Mist banner with a check badge — the kit's "All set!" notification. */
@Composable
fun SuccessBanner(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    SoftCard(modifier = modifier, color = Mist.copy(alpha = 0.55f).compositeOver(IvoryCard)) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 14.dp, bottom = 14.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Sage, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall.copy(fontSize = 16.sp), color = Ink)
                Text(message, style = MaterialTheme.typography.bodyMedium, color = InkMuted)
            }
            IconButton(onClick = onDismiss) {
                Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.dismiss), tint = InkMuted)
            }
        }
    }
}

private fun Color.compositeOver(background: Color): Color {
    val a = alpha
    return Color(
        red = red * a + background.red * (1 - a),
        green = green * a + background.green * (1 - a),
        blue = blue * a + background.blue * (1 - a),
        alpha = 1f
    )
}

/** While a screen shows the dark diary cover at the top, the status bar icons turn light. */
@Composable
fun CoverStatusBar() {
    val view = LocalView.current
    DisposableEffect(Unit) {
        val window = (view.context as? Activity)?.window
        val controller = window?.let { WindowCompat.getInsetsController(it, view) }
        controller?.isAppearanceLightStatusBars = false
        onDispose { controller?.isAppearanceLightStatusBars = true }
    }
}
