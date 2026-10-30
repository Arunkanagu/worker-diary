package dev.nura.app.thozhil.diary.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.automirrored.outlined.StickyNote2
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.nura.app.thozhil.diary.R
import dev.nura.app.thozhil.diary.data.NoteCounts
import dev.nura.app.thozhil.diary.data.SiteContact
import dev.nura.app.thozhil.diary.data.SiteNote
import dev.nura.app.thozhil.diary.ui.SiteViewModel
import dev.nura.app.thozhil.diary.ui.components.ClearFocusWhenClosed
import dev.nura.app.thozhil.diary.ui.components.ConfirmDialog
import dev.nura.app.thozhil.diary.ui.components.ContentMaxWidth
import dev.nura.app.thozhil.diary.ui.components.EmptyState
import dev.nura.app.thozhil.diary.ui.components.InitialsAvatar
import dev.nura.app.thozhil.diary.ui.components.ScreenHeader
import dev.nura.app.thozhil.diary.ui.components.SoftCard
import dev.nura.app.thozhil.diary.ui.theme.ErrorRed
import dev.nura.app.thozhil.diary.ui.theme.Ink
import dev.nura.app.thozhil.diary.ui.theme.InkMuted
import dev.nura.app.thozhil.diary.ui.theme.IvoryCard
import dev.nura.app.thozhil.diary.ui.theme.Mist
import dev.nura.app.thozhil.diary.ui.theme.SageDeep
import dev.nura.app.thozhil.diary.ui.theme.SageInk
import dev.nura.app.thozhil.diary.ui.theme.SandLight
import dev.nura.app.thozhil.diary.util.dial
import dev.nura.app.thozhil.diary.util.formatRupees
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import dev.nura.app.thozhil.diary.util.Language
import java.time.format.DateTimeFormatter

/** Keeps note titles short enough to read at a glance in the list. */
private const val NOTE_TITLE_MAX = 80

/** No-break spaces keep "27 Oct 2026" on one line in a card's detail line. */
private val noteDateFormat = DateTimeFormatter.ofPattern("d MMM yyyy")

@Composable
fun SiteDetailScreen(
    siteId: String,
    onBack: () -> Unit,
    onOpenNote: (String) -> Unit,
    viewModel: SiteViewModel = hiltViewModel()
) {
    val item by remember(siteId) { viewModel.observe(siteId) }.collectAsStateWithLifecycle(initialValue = null)
    val notes by remember(siteId) { viewModel.observeNotes(siteId) }.collectAsStateWithLifecycle(initialValue = emptyList())
    val counts by remember(siteId) { viewModel.observeNoteCounts(siteId) }.collectAsStateWithLifecycle(initialValue = emptyMap())
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
            .statusBarsPadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = ContentMaxWidth)
                .fillMaxSize()
        ) {
            ScreenHeader(
                title = item?.site?.name.orEmpty(),
                subtitle = item?.let {
                    when (it.contacts.size) {
                        0 -> stringResource(R.string.no_contacts)
                        1 -> stringResource(R.string.one_contact)
                        else -> stringResource(R.string.d_contacts, it.contacts.size)
                    }
                },
                onBack = onBack,
                trail = listOf(stringResource(R.string.sites)),
                modifier = Modifier.padding(start = 8.dp, end = 20.dp)
            )

            // One column on a phone, two or three on a tablet
            LazyVerticalGrid(
                columns = GridCells.Adaptive(NoteCardMinWidth),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 104.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                val contacts = item?.contacts.orEmpty()
                if (contacts.isNotEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        SoftCard(modifier = Modifier.fillMaxWidth()) {
                            contacts.forEachIndexed { index, contact ->
                                if (index > 0) HorizontalDivider(color = SandLight, modifier = Modifier.padding(horizontal = 16.dp))
                                ContactRow(contact)
                            }
                        }
                    }
                }

                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        if (notes.isEmpty()) stringResource(R.string.notes) else stringResource(R.string.notes_d, notes.size),
                        style = MaterialTheme.typography.titleMedium,
                        color = Ink,
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .semantics { heading() }
                    )
                }

                if (notes.isEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        EmptyState(
                            icon = Icons.AutoMirrored.Outlined.StickyNote2,
                            title = stringResource(R.string.no_notes_yet),
                            message = stringResource(R.string.tap_add_note_description),
                            modifier = Modifier.padding(top = 24.dp)
                        )
                    }
                } else {
                    items(notes, key = { it.id }) { note ->
                        NoteCard(
                            note = note,
                            counts = counts[note.id],
                            onOpen = { onOpenNote(note.id) },
                            onEditTitle = { openSheet(note) },
                            onDelete = { deleting = note }
                        )
                    }
                }
            }
        }

        Button(
            onClick = { openSheet(null) },
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .heightIn(min = 56.dp)
                .shadow(10.dp, CircleShape)
        ) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.add_note), style = MaterialTheme.typography.labelLarge)
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
            title = stringResource(R.string.delete_this_note_question),
            message = stringResource(R.string.bill_delete_warning, note.title.take(60)),
            confirmLabel = stringResource(R.string.delete),
            onConfirm = {
                viewModel.deleteNote(note)
                deleting = null
            },
            onDismiss = { deleting = null }
        )
    }
}

/** Cards are at least this wide, so a phone shows one column and a tablet two or three. */
internal val NoteCardMinWidth = 320.dp

/**
 * What a note or bill holds, e.g. "3 tables · 8 items". Each part keeps its words together
 * (no-break spaces), so a narrow card wraps at a "·" and never leaves "items" on a line alone.
 */
@Composable
private fun countsLabel(counts: NoteCounts?): String {
    fun together(text: String) = text.replace(' ', '\u00A0')
    val noTables = stringResource(R.string.no_tables_yet)
    val oneTable = stringResource(R.string.one_table)
    val oneItem = stringResource(R.string.one_item)
    return when {
        counts == null || counts.tables == 0 -> together(noTables)
        else -> {
            val tablesStr = if (counts.tables == 1) oneTable else stringResource(R.string.d_tables, counts.tables)
            val itemsStr = if (counts.items == 1) oneItem else stringResource(R.string.d_items, counts.items)
            together(tablesStr) + "\u00A0· " + together(itemsStr)
        }
    }
}

/**
 * A note or bill as a card: an icon, the title, when it was made and what it holds, and a ⋮ button
 * for Edit title and Delete (long press opens the same menu). A bill also shows its total below.
 * Tap opens it.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun NoteCard(
    note: SiteNote,
    counts: NoteCounts?,
    onOpen: () -> Unit,
    onEditTitle: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    /** A bill's total, shown in a footer. */
    total: Double? = null
) {
    val noun = stringResource(if (note.isBill) R.string.bill_lower else R.string.note_lower)
    var menuOpen by remember { mutableStateOf(false) }
    val haptics = LocalHapticFeedback.current
    val date = Instant.ofEpochMilli(note.createdAt).atZone(ZoneId.systemDefault()).format(noteDateFormat.withLocale(Language.locale))
    SoftCard(modifier = modifier.fillMaxWidth()) {
        Box {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        role = Role.Button,
                        onClickLabel = stringResource(R.string.open_s, noun),
                        onLongClickLabel = stringResource(R.string.s_options, noun),
                        onLongClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            menuOpen = true
                        },
                        onClick = onOpen
                    )
                    .padding(start = 16.dp, top = 14.dp, bottom = 14.dp, end = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Box(
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .size(44.dp)
                            .background(Mist, RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (note.isBill) Icons.AutoMirrored.Outlined.ReceiptLong else Icons.AutoMirrored.Outlined.StickyNote2,
                            contentDescription = null,
                            tint = SageInk,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(top = 2.dp)
                    ) {
                        Text(
                            note.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = Ink,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            "$date · ${countsLabel(counts)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = InkMuted,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(onClick = { menuOpen = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = stringResource(R.string.options_for_s, note.title), tint = InkMuted)
                    }
                }
                if (total != null) {
                    HorizontalDivider(color = SandLight, modifier = Modifier.padding(top = 12.dp, bottom = 10.dp, end = 12.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .semantics(mergeDescendants = true) {}
                    ) {
                        Text(
                            stringResource(R.string.bill_total),
                            style = MaterialTheme.typography.bodyMedium,
                            color = InkMuted,
                            modifier = Modifier.weight(1f)
                        )
                        Text(formatRupees(total), style = MaterialTheme.typography.titleLarge, color = SageDeep, maxLines = 1)
                    }
                }
            }
            DropdownMenu(
                expanded = menuOpen,
                onDismissRequest = { menuOpen = false },
                containerColor = IvoryCard
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.edit_title)) },
                    leadingIcon = { Icon(Icons.Outlined.Edit, contentDescription = null) },
                    onClick = {
                        menuOpen = false
                        onEditTitle()
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.delete), color = ErrorRed) },
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
    val noun = stringResource(if (isBill) R.string.bill_lower else R.string.note_lower)
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val focus = remember { FocusRequester() }
    var title by remember { mutableStateOf(existing?.title.orEmpty()) }
    var error by remember { mutableStateOf<String?>(null) }
    val titleRequiredError = stringResource(R.string.write_a_title_for_s, noun)

    // Animate the sheet away before removing it
    fun close(then: () -> Unit) {
        scope.launch { sheetState.hide() }.invokeOnCompletion { then() }
    }

    fun save() {
        val trimmed = title.trim()
        if (trimmed.isEmpty()) error = titleRequiredError else close { onSave(trimmed) }
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
                if (existing == null) stringResource(R.string.new_s, noun) else stringResource(R.string.edit_s, noun),
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
                label = { Text(if (isBill) stringResource(R.string.bill_title) else stringResource(R.string.note_title)) },
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
                    TextButton(onClick = { close(onDelete) }) { Text(stringResource(R.string.delete), color = ErrorRed) }
                }
                Spacer(Modifier.weight(1f))
                TextButton(onClick = { close(onDismiss) }) { Text(stringResource(R.string.cancel), color = SageDeep) }
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = { save() },
                    shape = CircleShape,
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text(if (existing == null) stringResource(R.string.add_s, noun) else stringResource(R.string.save))
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
                contact.mobile.ifEmpty { stringResource(R.string.no_mobile_number) },
                style = MaterialTheme.typography.bodyMedium,
                color = InkMuted
            )
        }
        if (contact.mobile.isNotEmpty()) {
            IconButton(onClick = { context.dial(contact.mobile) }) {
                Icon(Icons.Outlined.Call, contentDescription = stringResource(R.string.call_s, contact.name), tint = SageDeep)
            }
        }
    }
}
