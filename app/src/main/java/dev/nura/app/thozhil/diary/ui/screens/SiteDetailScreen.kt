package dev.nura.app.thozhil.diary.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.StickyNote2
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.nura.app.thozhil.diary.data.SiteContact
import dev.nura.app.thozhil.diary.data.SiteNote
import dev.nura.app.thozhil.diary.ui.SiteViewModel
import dev.nura.app.thozhil.diary.ui.components.ClearFocusWhenClosed
import dev.nura.app.thozhil.diary.ui.components.ConfirmDialog
import dev.nura.app.thozhil.diary.ui.components.EmptyState
import dev.nura.app.thozhil.diary.ui.components.InitialsAvatar
import dev.nura.app.thozhil.diary.ui.components.ScreenHeader
import dev.nura.app.thozhil.diary.ui.components.SoftCard
import dev.nura.app.thozhil.diary.ui.theme.ErrorRed
import dev.nura.app.thozhil.diary.ui.theme.Ink
import dev.nura.app.thozhil.diary.ui.theme.InkMuted
import dev.nura.app.thozhil.diary.ui.theme.IvoryCard
import dev.nura.app.thozhil.diary.ui.theme.SageDeep
import dev.nura.app.thozhil.diary.ui.theme.SandLight
import dev.nura.app.thozhil.diary.util.dial
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** Keeps note titles short enough to read at a glance in the list. */
private const val NOTE_TITLE_MAX = 80

private val noteTimeFormat = DateTimeFormatter.ofPattern("d MMM yyyy, h:mm a")

@Composable
fun SiteDetailScreen(
    siteId: Long,
    onBack: () -> Unit,
    onOpenNote: (Long) -> Unit,
    viewModel: SiteViewModel = hiltViewModel()
) {
    val item by remember(siteId) { viewModel.observe(siteId) }.collectAsStateWithLifecycle(initialValue = null)
    val notes by remember(siteId) { viewModel.observeNotes(siteId) }.collectAsStateWithLifecycle(initialValue = emptyList())
    var sheetOpen by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<SiteNote?>(null) }
    var deleting by remember { mutableStateOf<SiteNote?>(null) }

    fun openSheet(note: SiteNote?) {
        editing = note
        sheetOpen = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        LazyColumn(
            contentPadding = PaddingValues(bottom = 96.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                ScreenHeader(
                    title = item?.site?.name.orEmpty(),
                    subtitle = item?.let {
                        when (it.contacts.size) {
                            0 -> "No contacts"
                            1 -> "1 contact"
                            else -> "${it.contacts.size} contacts"
                        }
                    },
                    onBack = onBack,
                    modifier = Modifier.padding(start = 8.dp, end = 20.dp)
                )
            }

            val contacts = item?.contacts.orEmpty()
            if (contacts.isNotEmpty()) {
                item {
                    SoftCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                    ) {
                        contacts.forEachIndexed { index, contact ->
                            if (index > 0) HorizontalDivider(color = SandLight, modifier = Modifier.padding(horizontal = 16.dp))
                            ContactRow(contact)
                        }
                    }
                }
            }

            item {
                Text(
                    if (notes.isEmpty()) "Notes" else "Notes (${notes.size})",
                    style = MaterialTheme.typography.titleMedium,
                    color = Ink,
                    modifier = Modifier
                        .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 10.dp)
                        .semantics { heading() }
                )
            }

            if (notes.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.AutoMirrored.Outlined.StickyNote2,
                        title = "No notes yet",
                        message = "Tap Add note to write down work, materials or anything to remember for this site.",
                        modifier = Modifier.padding(top = 24.dp)
                    )
                }
            } else {
                itemsIndexed(notes, key = { _, note -> note.id }) { index, note ->
                    NoteRow(
                        note = note,
                        first = index == 0,
                        last = index == notes.lastIndex,
                        onOpen = { onOpenNote(note.id) },
                        onEditTitle = { openSheet(note) },
                        onDelete = { deleting = note }
                    )
                }
            }
        }

        Button(
            onClick = { openSheet(null) },
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .height(56.dp)
                .shadow(10.dp, CircleShape)
        ) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Add note", style = MaterialTheme.typography.labelLarge)
        }
    }

    ClearFocusWhenClosed(open = sheetOpen || deleting != null)

    if (sheetOpen) {
        NoteSheet(
            existing = editing,
            onDismiss = { sheetOpen = false },
            onSave = { title ->
                viewModel.saveNote(editing?.copy(title = title) ?: SiteNote(siteId = siteId, title = title))
                sheetOpen = false
            },
            onDelete = {
                deleting = editing
                sheetOpen = false
            }
        )
    }

    deleting?.let { note ->
        ConfirmDialog(
            title = "Delete this note?",
            message = "\"${note.title.take(60)}\" and its items will be removed. This can't be undone.",
            confirmLabel = "Delete",
            onConfirm = {
                viewModel.deleteNote(note)
                deleting = null
            },
            onDismiss = { deleting = null }
        )
    }
}

/**
 * Notes sit on one continuous sheet: rows share a card, with the corners rounded only at the
 * top of the first row and the bottom of the last. Tap opens the note; long press shows options.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun NoteRow(
    note: SiteNote,
    first: Boolean,
    last: Boolean,
    onOpen: () -> Unit,
    onEditTitle: () -> Unit,
    onDelete: () -> Unit,
    /** Shown at the end of the row, e.g. a bill's total. */
    trailing: String? = null
) {
    val noun = if (note.isBill) "bill" else "note"
    var menuOpen by remember { mutableStateOf(false) }
    val haptics = LocalHapticFeedback.current
    val corner = 24.dp
    val shape = RoundedCornerShape(
        topStart = if (first) corner else 0.dp,
        topEnd = if (first) corner else 0.dp,
        bottomStart = if (last) corner else 0.dp,
        bottomEnd = if (last) corner else 0.dp
    )
    SoftCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = shape,
        highlightEdge = false
    ) {
        if (!first) HorizontalDivider(color = SandLight, modifier = Modifier.padding(horizontal = 16.dp))
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        role = Role.Button,
                        onClickLabel = "Open $noun",
                        onLongClickLabel = "${noun.replaceFirstChar { it.uppercase() }} options",
                        onLongClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            menuOpen = true
                        },
                        onClick = onOpen
                    )
                    .padding(start = 18.dp, end = 18.dp, top = 14.dp, bottom = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        note.title,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Ink,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        Instant.ofEpochMilli(note.createdAt).atZone(ZoneId.systemDefault()).format(noteTimeFormat),
                        style = MaterialTheme.typography.bodySmall,
                        color = InkMuted
                    )
                }
                if (trailing != null) {
                    Spacer(Modifier.width(12.dp))
                    Text(trailing, style = MaterialTheme.typography.titleSmall, color = SageDeep)
                }
            }
            DropdownMenu(
                expanded = menuOpen,
                onDismissRequest = { menuOpen = false },
                containerColor = IvoryCard
            ) {
                DropdownMenuItem(
                    text = { Text("Edit title") },
                    leadingIcon = { Icon(Icons.Outlined.Edit, contentDescription = null) },
                    onClick = {
                        menuOpen = false
                        onEditTitle()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Delete", color = ErrorRed) },
                    leadingIcon = { Icon(Icons.Outlined.DeleteOutline, contentDescription = null, tint = ErrorRed) },
                    onClick = {
                        menuOpen = false
                        onDelete()
                    }
                )
            }
        }
    }
}

/** Title sheet for a new or existing note, or a bill when [isBill]. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NoteSheet(
    existing: SiteNote?,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    onDelete: () -> Unit,
    isBill: Boolean = false
) {
    val noun = if (isBill) "bill" else "note"
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val focus = remember { FocusRequester() }
    var title by remember { mutableStateOf(existing?.title.orEmpty()) }
    var error by remember { mutableStateOf<String?>(null) }

    // Animate the sheet away before removing it
    fun close(then: () -> Unit) {
        scope.launch { sheetState.hide() }.invokeOnCompletion { then() }
    }

    fun save() {
        val trimmed = title.trim()
        if (trimmed.isEmpty()) error = "Write a title for the $noun" else close { onSave(trimmed) }
    }

    LaunchedEffect(Unit) { focus.requestFocus() }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = IvoryCard
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(start = 20.dp, end = 20.dp, bottom = 16.dp)
        ) {
            Text(
                if (existing == null) "New $noun" else "Edit $noun",
                style = MaterialTheme.typography.titleLarge,
                color = Ink
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it.take(NOTE_TITLE_MAX)
                    error = null
                },
                label = { Text(if (isBill) "Bill title" else "Note title") },
                isError = error != null,
                supportingText = { Text(error ?: "${title.length} / $NOTE_TITLE_MAX") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { save() }),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focus)
            )
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (existing != null) {
                    TextButton(onClick = { close(onDelete) }) { Text("Delete", color = ErrorRed) }
                }
                Spacer(Modifier.weight(1f))
                TextButton(onClick = { close(onDismiss) }) { Text("Cancel", color = SageDeep) }
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = { save() },
                    shape = CircleShape,
                    modifier = Modifier.height(48.dp)
                ) {
                    Text(if (existing == null) "Add $noun" else "Save")
                }
            }
        }
    }
}

@Composable
private fun ContactRow(contact: SiteContact) {
    val context = LocalContext.current
    Row(
        modifier = Modifier.padding(start = 16.dp, top = 10.dp, bottom = 10.dp, end = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        InitialsAvatar(name = contact.name, size = 40.dp, showDot = false)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                contact.name,
                style = MaterialTheme.typography.titleSmall,
                color = Ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                contact.mobile.ifEmpty { "No mobile number" },
                style = MaterialTheme.typography.bodyMedium,
                color = InkMuted
            )
        }
        if (contact.mobile.isNotEmpty()) {
            IconButton(onClick = { context.dial(contact.mobile) }) {
                Icon(Icons.Outlined.Call, contentDescription = "Call ${contact.name}", tint = SageDeep)
            }
        }
    }
}
