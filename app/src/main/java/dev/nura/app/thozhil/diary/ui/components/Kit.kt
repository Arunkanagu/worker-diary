package dev.nura.app.thozhil.diary.ui.components

import androidx.annotation.StringRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.nura.app.thozhil.diary.R
import dev.nura.app.thozhil.diary.ui.theme.Cover
import dev.nura.app.thozhil.diary.ui.theme.ErrorRed
import dev.nura.app.thozhil.diary.ui.theme.Ink
import dev.nura.app.thozhil.diary.ui.theme.InkMuted
import dev.nura.app.thozhil.diary.ui.theme.Ivory
import dev.nura.app.thozhil.diary.ui.theme.IvoryCard
import dev.nura.app.thozhil.diary.ui.theme.Mist
import dev.nura.app.thozhil.diary.ui.theme.Moss
import dev.nura.app.thozhil.diary.ui.theme.OnCoverMuted
import dev.nura.app.thozhil.diary.ui.theme.SageDeep
import dev.nura.app.thozhil.diary.ui.theme.SageInk
import dev.nura.app.thozhil.diary.ui.theme.Sand
import kotlinx.coroutines.delay

// ---------------------------------------------------------------- Bottom navigation

data class BottomNavItem(
    val route: String,
    @param:StringRes val labelRes: Int,
    val icon: ImageVector,
    val selectedIcon: ImageVector
)

/**
 * The phone dock: a floating bar in the diary-cover green, icons only. The selected tab sits
 * in a paper pill. Labels are kept for screen readers.
 */
@Composable
fun CalmBottomBar(
    items: List<BottomNavItem>,
    currentRoute: String?,
    onSelect: (BottomNavItem) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 20.dp, end = 20.dp, bottom = 12.dp, top = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        // On a portrait tablet the dock keeps a phone-like width instead of spreading the icons apart
        Row(
            modifier = Modifier
                .widthIn(max = 520.dp)
                .fillMaxWidth()
                .shadow(14.dp, CircleShape, ambientColor = Cover.copy(alpha = 0.3f), spotColor = Cover.copy(alpha = 0.35f))
                .clip(CircleShape)
                .background(Cover)
                .padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val selected = item.route == currentRoute
                val pill by animateColorAsState(
                    if (selected) Ivory else Color.Transparent,
                    animationSpec = tween(240),
                    label = "navPill"
                )
                val tint by animateColorAsState(
                    if (selected) SageDeep else OnCoverMuted,
                    animationSpec = tween(240),
                    label = "navTint"
                )
                val labelText = stringResource(item.labelRes)
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .clip(CircleShape)
                        .background(pill)
                        .clickable(role = Role.Tab) { onSelect(item) }
                        .semantics {
                            contentDescription = labelText
                            this.selected = selected
                        },
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (selected) item.selectedIcon else item.icon,
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }
    }
}

/** Screens at least this wide get the side rail and two-column layouts. */
val WideLayoutMinWidth = 840.dp

/** Content stops growing past this so lists and forms don't stretch edge to edge on tablets. */
val ContentMaxWidth = 960.dp

/**
 * Side rail for wide screens: the same sage pill as the bottom bar, stacked down the left edge.
 * There is room here, so each tab shows its label under the icon.
 */
@Composable
fun CalmNavRail(
    items: List<BottomNavItem>,
    currentRoute: String?,
    onSelect: (BottomNavItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(start = 16.dp, end = 8.dp, top = 24.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AppLogo(size = 44.dp)
        Spacer(Modifier.height(20.dp))
        items.forEach { item ->
            val selected = item.route == currentRoute
            val pill by animateColorAsState(
                if (selected) SageDeep else Color.Transparent,
                animationSpec = tween(220),
                label = "railPill"
            )
            val tint by animateColorAsState(
                if (selected) Color.White else InkMuted,
                animationSpec = tween(220),
                label = "railTint"
            )
            Column(
                modifier = Modifier
                    .width(96.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .clickable(role = Role.Tab) { onSelect(item) }
                    .semantics { this.selected = selected }
                    .padding(vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 64.dp, height = 40.dp)
                        .clip(CircleShape)
                        .background(pill),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (selected) item.selectedIcon else item.icon,
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    stringResource(item.labelRes),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (selected) SageDeep else InkMuted,
                    maxLines = 1
                )
            }
        }
    }
}

// ---------------------------------------------------------------- Headers

/** From this width a header also shows where the page sits, e.g. "Bills › Green Valley Villa". */
private val TrailMinWidth = 600.dp

/**
 * Page title with an optional back arrow and trailing actions. On a tablet, [trail] (the pages above
 * this one, outermost first) shows above the title so it's clear where you are.
 */
@Composable
fun ScreenHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    trail: List<String> = emptyList(),
    actions: @Composable RowScope.() -> Unit = {}
) {
    val showTrail = trail.isNotEmpty() && LocalConfiguration.current.screenWidthDp.dp >= TrailMinWidth
    val inPrefix = stringResource(R.string.in_prefix)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back), tint = Ink)
            }
            Spacer(Modifier.width(4.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            if (showTrail) {
                Text(
                    trail.filter { it.isNotBlank() }.joinToString("  ›  "),
                    style = MaterialTheme.typography.labelMedium,
                    color = InkMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.semantics { contentDescription = inPrefix + trail.filter { it.isNotBlank() }.joinToString(", ") }
                )
            }
            Text(
                title,
                style = if (onBack != null) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineMedium,
                color = Ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = InkMuted)
            }
        }
        actions()
    }
}

// ---------------------------------------------------------------- Inputs

/** Segmented tabs: sage fill on the selected option. */
@Composable
fun CalmSegmented(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    icons: List<ImageVector>? = null
) {
    SoftCard(modifier = modifier, shape = RoundedCornerShape(18.dp)) {
        Row(modifier = Modifier.padding(4.dp)) {
            options.forEachIndexed { index, label ->
                val selected = index == selectedIndex
                val bg by animateColorAsState(if (selected) SageDeep else Color.Transparent, tween(200), label = "segBg")
                val fg by animateColorAsState(if (selected) Color.White else Ink, tween(200), label = "segFg")
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(bg)
                        .clickable(role = Role.Tab) { onSelect(index) }
                        .semantics { this.selected = selected }
                        .heightIn(min = 44.dp)
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (icons != null) {
                        Icon(icons[index], contentDescription = null, tint = fg, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                    }
                    Text(label, style = MaterialTheme.typography.labelMedium, color = fg, maxLines = 1)
                }
            }
        }
    }
}

/** Pill search input like the kit's search field. */
@Composable
fun CalmSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
        trailingIcon = {
            if (value.isNotEmpty()) {
                IconButton(onClick = { onValueChange("") }) {
                    Icon(Icons.Filled.Clear, contentDescription = stringResource(R.string.clear_search))
                }
            }
        },
        singleLine = true,
        shape = CircleShape,
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = IvoryCard,
            focusedContainerColor = IvoryCard,
            unfocusedBorderColor = Sand,
            focusedBorderColor = SageDeep,
            unfocusedLeadingIconColor = InkMuted,
            focusedLeadingIconColor = SageDeep
        ),
        modifier = modifier.fillMaxWidth()
    )
}

// ---------------------------------------------------------------- Display

/** Small number tile used for day / month stats. */
@Composable
fun StatTile(value: String, label: String, modifier: Modifier = Modifier, accent: Color = Ink) {
    SoftCard(modifier = modifier, shape = RoundedCornerShape(18.dp)) {
        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp)) {
            Text(value, style = MaterialTheme.typography.titleLarge, color = accent, maxLines = 1)
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = InkMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun StatusPill(text: String, container: Color, content: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(container)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text, style = MaterialTheme.typography.labelSmall, color = content, maxLines = 1)
    }
}

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    action: @Composable ColumnScope.() -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(76.dp)
                .background(Mist, RoundedCornerShape(24.dp))
                .border(1.dp, Moss.copy(alpha = 0.5f), RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = SageInk, modifier = Modifier.size(34.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, color = Ink, textAlign = TextAlign.Center)
        Spacer(Modifier.height(4.dp))
        Text(message, style = MaterialTheme.typography.bodyMedium, color = InkMuted, textAlign = TextAlign.Center)
        Spacer(Modifier.height(20.dp))
        action()
    }
}

/**
 * When a dialog or sheet closes, Android hands focus back to the first text field on the
 * screen (opening the keyboard). Drop that focus so the keyboard stays hidden.
 */
@Composable
fun ClearFocusWhenClosed(open: Boolean) {
    val focusManager = LocalFocusManager.current
    LaunchedEffect(open) {
        if (!open) {
            delay(150)
            focusManager.clearFocus()
        }
    }
}

/** Confirmation for destructive actions. */
@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = IvoryCard,
        title = { Text(title, color = Ink) },
        text = { Text(message, color = InkMuted) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = ErrorRed, contentColor = Color.White),
                shape = CircleShape
            ) { Text(confirmLabel) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel), color = SageDeep) }
        }
    )
}
