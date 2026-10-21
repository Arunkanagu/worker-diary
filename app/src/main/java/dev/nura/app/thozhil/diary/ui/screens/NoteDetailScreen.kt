package dev.nura.app.thozhil.diary.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.nura.app.thozhil.diary.data.NoteItem
import dev.nura.app.thozhil.diary.data.NoteTable
import dev.nura.app.thozhil.diary.data.SizeUnit
import dev.nura.app.thozhil.diary.data.amount
import dev.nura.app.thozhil.diary.data.formatSize
import dev.nura.app.thozhil.diary.ui.NoteViewModel
import dev.nura.app.thozhil.diary.ui.TableWithItems
import dev.nura.app.thozhil.diary.ui.components.CalmSegmented
import dev.nura.app.thozhil.diary.ui.components.ClearFocusWhenClosed
import dev.nura.app.thozhil.diary.ui.components.ConfirmDialog
import dev.nura.app.thozhil.diary.ui.components.EmptyState
import dev.nura.app.thozhil.diary.ui.components.ScreenHeader
import dev.nura.app.thozhil.diary.ui.components.SoftCard
import dev.nura.app.thozhil.diary.ui.theme.ErrorRed
import dev.nura.app.thozhil.diary.ui.theme.Ink
import dev.nura.app.thozhil.diary.ui.theme.InkMuted
import dev.nura.app.thozhil.diary.ui.theme.IvoryCard
import dev.nura.app.thozhil.diary.ui.theme.Mist
import dev.nura.app.thozhil.diary.ui.theme.SageDeep
import dev.nura.app.thozhil.diary.ui.theme.SageInk
import dev.nura.app.thozhil.diary.ui.theme.Sand
import dev.nura.app.thozhil.diary.ui.theme.SandLight
import dev.nura.app.thozhil.diary.util.NotePdf
import dev.nura.app.thozhil.diary.util.formatRupees
import dev.nura.app.thozhil.diary.util.plainNumber
import dev.nura.app.thozhil.diary.util.sharePdf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Table columns, in order, with fixed widths so rows line up while scrolling sideways. */
private enum class TableColumn(val label: String, val width: Dp, val numeric: Boolean = false) {
    NUMBER("#", 30.dp, numeric = true),
    ITEM("Item", 124.dp),
    THICKNESS("Thickness", 88.dp, numeric = true),
    WIDTH("Width", 80.dp, numeric = true),
    LENGTH("Length", 80.dp, numeric = true),
    QUANTITY("Qty", 64.dp, numeric = true),
    RATE("Rate", 88.dp, numeric = true),
    AMOUNT("Amount", 104.dp, numeric = true),
    NOTES("Notes", 160.dp)
}

/** Bills add Rate and Amount; notes leave them out. */
private fun columnsFor(isBill: Boolean) =
    if (isBill) TableColumn.entries else TableColumn.entries - setOf(TableColumn.RATE, TableColumn.AMOUNT)

@Composable
fun NoteDetailScreen(
    onBack: () -> Unit,
    viewModel: NoteViewModel = hiltViewModel()
) {
    val note by viewModel.note.collectAsStateWithLifecycle()
    val site by viewModel.site.collectAsStateWithLifecycle()
    val tables by viewModel.tables.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Which table the item sheet adds to, and the row being edited (null when adding)
    var itemSheetTable by remember { mutableStateOf<NoteTable?>(null) }
    var editingItem by remember { mutableStateOf<NoteItem?>(null) }
    var deletingItem by remember { mutableStateOf<NoteItem?>(null) }
    var tableSheetOpen by remember { mutableStateOf(false) }
    var editingTable by remember { mutableStateOf<NoteTable?>(null) }
    var deletingTable by remember { mutableStateOf<TableWithItems?>(null) }
    var exporting by remember { mutableStateOf(false) }

    val isBill = note?.isBill == true
    val allItems = tables.flatMap { it.items }
    val totalPieces = allItems.sumOf { it.quantity ?: 0 }
    val totalAmount = allItems.sumOf { it.amount() ?: 0.0 }

    fun openItemSheet(table: NoteTable, item: NoteItem?) {
        editingItem = item
        itemSheetTable = table
    }

    fun openTableSheet(table: NoteTable?) {
        editingTable = table
        tableSheetOpen = true
    }

    fun exportPdf() {
        val current = note ?: return
        scope.launch {
            exporting = true
            try {
                val file = withContext(Dispatchers.IO) { NotePdf.write(context, site, current, tables) }
                context.sharePdf(file)
            } catch (e: Exception) {
                Toast.makeText(context, "Couldn't make the PDF: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                exporting = false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 120.dp)
        ) {
            ScreenHeader(
                title = note?.title.orEmpty(),
                subtitle = when {
                    tables.isEmpty() -> site?.site?.name
                    allItems.isEmpty() -> "${site?.site?.name.orEmpty()}, no items yet"
                    isBill -> "${site?.site?.name.orEmpty()}, ${allItems.size} items, ${formatRupees(totalAmount)}"
                    else -> "${site?.site?.name.orEmpty()}, ${allItems.size} items, $totalPieces pcs"
                },
                onBack = onBack,
                modifier = Modifier.padding(start = 8.dp, end = 8.dp)
            ) {
                if (exporting) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        color = SageDeep,
                        modifier = Modifier
                            .padding(12.dp)
                            .size(24.dp)
                    )
                } else {
                    IconButton(onClick = ::exportPdf, enabled = note != null) {
                        Icon(Icons.Outlined.PictureAsPdf, contentDescription = "Share as PDF", tint = SageDeep)
                    }
                }
            }

            if (tables.isEmpty()) {
                EmptyState(
                    icon = Icons.Outlined.TableChart,
                    title = "No tables yet",
                    message = if (isBill) {
                        "Tap New table to start one, for example Doors. Add items with a rate and the amounts add up."
                    } else {
                        "Tap New table to start one, for example Ground floor windows. Each table has its own items."
                    },
                    modifier = Modifier.padding(top = 48.dp)
                )
            }

            tables.forEach { entry ->
                TableSection(
                    entry = entry,
                    isBill = isBill,
                    onAddItem = { openItemSheet(entry.table, null) },
                    onRowClick = { openItemSheet(entry.table, it) },
                    onRename = { openTableSheet(entry.table) },
                    onDelete = { deletingTable = entry }
                )
            }
        }

        Button(
            onClick = { openTableSheet(null) },
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .height(56.dp)
                .shadow(10.dp, CircleShape)
        ) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("New table", style = MaterialTheme.typography.labelLarge)
        }
    }

    ClearFocusWhenClosed(
        open = itemSheetTable != null || tableSheetOpen || deletingItem != null || deletingTable != null
    )

    itemSheetTable?.let { table ->
        // New rows start with the units used last in this table, since a table measures similar pieces
        val last = tables.firstOrNull { it.table.id == table.id }?.items?.lastOrNull()
        ItemSheet(
            tableName = table.name,
            isBill = isBill,
            existing = editingItem,
            defaults = last?.let {
                NoteItem(noteId = 0, item = "", thicknessUnit = it.thicknessUnit, widthUnit = it.widthUnit, lengthUnit = it.lengthUnit)
            },
            onDismiss = { itemSheetTable = null },
            onSave = {
                viewModel.saveItem(it.copy(tableId = table.id))
                // Adding keeps the sheet open for the next row; editing closes it
                if (editingItem != null) itemSheetTable = null
            },
            onDelete = {
                deletingItem = editingItem
                itemSheetTable = null
            }
        )
    }

    if (tableSheetOpen) {
        TableNameSheet(
            existing = editingTable,
            suggestedName = "Table ${tables.size + 1}",
            onDismiss = { tableSheetOpen = false },
            onSave = { name ->
                viewModel.saveTable(editingTable?.copy(name = name) ?: NoteTable(noteId = 0, name = name))
                tableSheetOpen = false
            }
        )
    }

    deletingItem?.let { item ->
        ConfirmDialog(
            title = "Delete ${item.item}?",
            message = "This row will be removed from the table. This can't be undone.",
            confirmLabel = "Delete",
            onConfirm = {
                viewModel.deleteItem(item)
                deletingItem = null
            },
            onDismiss = { deletingItem = null }
        )
    }

    deletingTable?.let { entry ->
        ConfirmDialog(
            title = "Delete ${entry.table.name}?",
            message = when (entry.items.size) {
                0 -> "This empty table will be removed."
                1 -> "The table and its 1 item will be removed. This can't be undone."
                else -> "The table and its ${entry.items.size} items will be removed. This can't be undone."
            },
            confirmLabel = "Delete",
            onConfirm = {
                viewModel.deleteTable(entry.table)
                deletingTable = null
            },
            onDismiss = { deletingTable = null }
        )
    }
}

/** A table's name, summary and options, then its rows and an Add item button. */
@Composable
private fun TableSection(
    entry: TableWithItems,
    isBill: Boolean,
    onAddItem: () -> Unit,
    onRowClick: (NoteItem) -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    val (table, items) = entry
    val pieces = items.sumOf { it.quantity ?: 0 }
    val amount = items.sumOf { it.amount() ?: 0.0 }
    var menuOpen by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(top = 16.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 20.dp, end = 8.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    table.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = Ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.semantics { heading() }
                )
                Text(
                    when (items.size) {
                        0 -> "No items"
                        1 -> if (isBill) "1 item, ${formatRupees(amount)}" else "1 item, $pieces pcs"
                        else -> if (isBill) "${items.size} items, ${formatRupees(amount)}" else "${items.size} items, $pieces pcs"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = InkMuted
                )
            }
            Box {
                IconButton(onClick = { menuOpen = true }) {
                    Icon(Icons.Filled.MoreHoriz, contentDescription = "Options for ${table.name}", tint = Ink)
                }
                DropdownMenu(
                    expanded = menuOpen,
                    onDismissRequest = { menuOpen = false },
                    containerColor = IvoryCard
                ) {
                    DropdownMenuItem(
                        text = { Text("Rename") },
                        leadingIcon = { Icon(Icons.Outlined.Edit, contentDescription = null) },
                        onClick = {
                            menuOpen = false
                            onRename()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete table", color = ErrorRed) },
                        leadingIcon = { Icon(Icons.Outlined.DeleteOutline, contentDescription = null, tint = ErrorRed) },
                        onClick = {
                            menuOpen = false
                            onDelete()
                        }
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        if (items.isNotEmpty()) {
            ItemsTable(
                items = items,
                isBill = isBill,
                totalPieces = pieces,
                onRowClick = onRowClick,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }
        OutlinedButton(
            onClick = onAddItem,
            shape = CircleShape,
            border = BorderStroke(1.dp, Sand),
            modifier = Modifier
                .padding(start = 20.dp, end = 20.dp, top = 10.dp)
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = null, tint = SageDeep)
            Spacer(Modifier.width(8.dp))
            Text("Add item to ${table.name}", color = SageDeep, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

/** Name a new table, or rename one. A blank name falls back to [suggestedName]. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TableNameSheet(
    existing: NoteTable?,
    suggestedName: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val focus = remember { FocusRequester() }
    var name by remember { mutableStateOf(existing?.name.orEmpty()) }

    fun close(then: () -> Unit) {
        scope.launch { sheetState.hide() }.invokeOnCompletion { then() }
    }

    fun save() {
        val chosen = name.trim().ifEmpty { existing?.name ?: suggestedName }
        close { onSave(chosen) }
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
                if (existing == null) "New table" else "Rename table",
                style = MaterialTheme.typography.titleLarge,
                color = Ink
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it.take(60) },
                label = { Text("Table name") },
                placeholder = { Text(suggestedName) },
                supportingText = { Text("For example Ground floor windows") },
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
                Spacer(Modifier.weight(1f))
                TextButton(onClick = { close(onDismiss) }) { Text("Cancel", color = SageDeep) }
                Spacer(Modifier.width(8.dp))
                Button(onClick = { save() }, shape = CircleShape, modifier = Modifier.height(48.dp)) {
                    Text(if (existing == null) "Create table" else "Save")
                }
            }
        }
    }
}

/** Accepts 2, 2.5, 1/8, 2.1/8 or 2 1/8 (the last two meaning 2⅛). */
private val sizePattern = Regex("""^\d+(\.\d+)?$|^(\d+[. ])?\d+/[1-9]\d*$""")

private fun sizeError(value: String) =
    if (value.isEmpty() || sizePattern.matches(value)) null else "Use a size like 2, 2.5 or 2.1/8"

/** Keeps digits, '.', '/' and single spaces, so fractions like 2.1/8 can be typed. */
private fun sizeInput(input: String): String =
    input.filter { it.isDigit() || it == '.' || it == '/' || it == ' ' }
        .replace(Regex(" +"), " ")
        .trimStart()
        .take(10)

private val TableHeaderHeight = 40.dp
private val TableRowHeight = 52.dp
private val TableTotalHeight = 48.dp

/**
 * # and Item stay pinned on the left while the size, quantity and notes columns scroll sideways.
 * Rows have a fixed height so both halves line up; a fade on the right shows there is more to see.
 */
@Composable
private fun ItemsTable(
    items: List<NoteItem>,
    isBill: Boolean,
    totalPieces: Int,
    onRowClick: (NoteItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val pinned = listOf(TableColumn.NUMBER, TableColumn.ITEM)
    val scrolling = columnsFor(isBill) - pinned.toSet()
    val scroll = rememberScrollState()

    SoftCard(modifier = modifier.fillMaxWidth()) {
        Row {
            TableHalf(pinned, items, isBill, totalPieces, onRowClick)
            if (scroll.value > 0) {
                // Edge between the pinned and scrolled columns once content slides under it
                Box(
                    Modifier
                        .width(1.dp)
                        .height(TableHeaderHeight + TableRowHeight * items.size + TableTotalHeight)
                        .background(SandLight)
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                Box(modifier = Modifier.horizontalScroll(scroll)) {
                    TableHalf(scrolling, items, isBill, totalPieces, onRowClick)
                }
                if (scroll.canScrollForward) {
                    Box(modifier = Modifier.matchParentSize(), contentAlignment = Alignment.CenterEnd) {
                        Box(
                            Modifier
                                .fillMaxHeight()
                                .width(28.dp)
                                .background(Brush.horizontalGradient(listOf(IvoryCard.copy(alpha = 0f), IvoryCard)))
                        )
                    }
                }
            }
        }
    }
}

/** One side of the table: header, a row per item, then the total row. */
@Composable
private fun TableHalf(
    columns: List<TableColumn>,
    items: List<NoteItem>,
    isBill: Boolean,
    totalPieces: Int,
    onRowClick: (NoteItem) -> Unit
) {
    val totalAmount = items.sumOf { it.amount() ?: 0.0 }
    // Width follows the cells; without this the row dividers would stretch the half to the full card
    Column(modifier = Modifier.width(IntrinsicSize.Max)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .height(TableHeaderHeight)
                .background(SandLight)
                .padding(horizontal = 4.dp)
        ) {
            columns.forEach { col ->
                Cell(col) {
                    Text(
                        col.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = InkMuted,
                        textAlign = if (col.numeric) TextAlign.End else TextAlign.Start,
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        items.forEachIndexed { index, item ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .height(TableRowHeight)
                    .clickable(role = Role.Button, onClickLabel = "Change ${item.item}") { onRowClick(item) }
                    .padding(horizontal = 4.dp)
            ) {
                columns.forEach { col ->
                    Cell(col) {
                        val text = when (col) {
                            TableColumn.NUMBER -> "${index + 1}"
                            TableColumn.ITEM -> item.item
                            TableColumn.THICKNESS -> formatSize(item.thickness, item.thicknessUnit)
                            TableColumn.WIDTH -> formatSize(item.width, item.widthUnit)
                            TableColumn.LENGTH -> formatSize(item.length, item.lengthUnit)
                            TableColumn.QUANTITY -> item.quantity?.toString() ?: "–"
                            TableColumn.RATE -> item.rate?.let(::formatRupees) ?: "–"
                            TableColumn.AMOUNT -> item.amount()?.let(::formatRupees) ?: "–"
                            TableColumn.NOTES -> item.remarks.ifEmpty { "–" }
                        }
                        Text(
                            text,
                            style = if (col == TableColumn.ITEM) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
                            color = if (col == TableColumn.NUMBER || col == TableColumn.NOTES) InkMuted else Ink,
                            textAlign = if (col.numeric) TextAlign.End else TextAlign.Start,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            HorizontalDivider(color = SandLight)
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .height(TableTotalHeight - 1.dp)
                .padding(horizontal = 4.dp)
        ) {
            columns.forEach { col ->
                Cell(col) {
                    when (col) {
                        // The count sits beside "Total" too, so it shows without scrolling
                        TableColumn.ITEM -> Text(
                            if (isBill) "Total  ${formatRupees(totalAmount)}" else "Total  $totalPieces pcs",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = SageDeep,
                            maxLines = 1
                        )
                        TableColumn.QUANTITY, TableColumn.AMOUNT -> Text(
                            if (col == TableColumn.AMOUNT) formatRupees(totalAmount) else "$totalPieces",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = SageDeep,
                            textAlign = TextAlign.End,
                            maxLines = 1,
                            modifier = Modifier.fillMaxWidth()
                        )
                        else -> {}
                    }
                }
            }
        }
    }
}

@Composable
private fun Cell(col: TableColumn, content: @Composable () -> Unit) {
    Box(modifier = Modifier.width(col.width).padding(horizontal = 6.dp)) { content() }
}

/**
 * Adding keeps the sheet open and clears it for the next row (units stay as chosen);
 * editing saves and closes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ItemSheet(
    tableName: String,
    isBill: Boolean,
    existing: NoteItem?,
    defaults: NoteItem?,
    onDismiss: () -> Unit,
    onSave: (NoteItem) -> Unit,
    onDelete: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val itemFocus = remember { FocusRequester() }
    val start = existing ?: defaults ?: NoteItem(noteId = 0, item = "")

    var item by remember { mutableStateOf(existing?.item.orEmpty()) }
    var itemError by remember { mutableStateOf<String?>(null) }
    var thickness by remember { mutableStateOf(existing?.thickness.orEmpty()) }
    var thicknessUnit by remember { mutableStateOf(start.thicknessUnit) }
    var width by remember { mutableStateOf(existing?.width.orEmpty()) }
    var widthUnit by remember { mutableStateOf(start.widthUnit) }
    var length by remember { mutableStateOf(existing?.length.orEmpty()) }
    var lengthUnit by remember { mutableStateOf(start.lengthUnit) }
    // New rows start at 1 piece
    var quantity by remember { mutableStateOf(if (existing == null) "1" else existing.quantity?.toString().orEmpty()) }
    var rate by remember { mutableStateOf(existing?.rate?.let(::plainNumber).orEmpty()) }
    var remarks by remember { mutableStateOf(existing?.remarks.orEmpty()) }
    var showSizeErrors by remember { mutableStateOf(false) }
    var lastAdded by remember { mutableStateOf<String?>(null) }
    var addedCount by remember { mutableIntStateOf(0) }

    fun close(then: () -> Unit) {
        scope.launch { sheetState.hide() }.invokeOnCompletion { then() }
    }

    fun save() {
        val name = item.trim()
        itemError = if (name.isEmpty()) "Please enter the item" else null
        showSizeErrors = true
        val sizes = listOf(thickness, width, length).map { it.trim() }
        if (itemError != null || sizes.any { sizeError(it) != null }) return
        val saved = (existing ?: NoteItem(noteId = 0, item = name)).copy(
            item = name,
            thickness = sizes[0],
            thicknessUnit = thicknessUnit,
            width = sizes[1],
            widthUnit = widthUnit,
            length = sizes[2],
            lengthUnit = lengthUnit,
            quantity = quantity.toIntOrNull(),
            rate = if (isBill) rate.toDoubleOrNull() else existing?.rate,
            remarks = remarks.trim()
        )
        if (existing != null) {
            close { onSave(saved) }
            return
        }
        onSave(saved)
        addedCount++
        lastAdded = name
        item = ""
        thickness = ""
        width = ""
        length = ""
        quantity = "1"
        rate = ""
        remarks = ""
        showSizeErrors = false
        itemFocus.requestFocus()
    }

    LaunchedEffect(Unit) { if (existing == null) itemFocus.requestFocus() }

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
        ) {
            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
                    .padding(start = 20.dp, end = 20.dp, bottom = 8.dp)
            ) {
                Text(
                    if (existing == null) "New item" else "Edit item",
                    style = MaterialTheme.typography.titleLarge,
                    color = Ink
                )
                Text(tableName, style = MaterialTheme.typography.bodyMedium, color = InkMuted)
                lastAdded?.let {
                    Spacer(Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = null, tint = SageDeep, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            if (addedCount == 1) "Added $it" else "Added $it, $addedCount added so far",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SageDeep,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = item,
                    onValueChange = {
                        item = it
                        itemError = null
                    },
                    label = { Text("Item") },
                    isError = itemError != null,
                    supportingText = itemError?.let { { Text(it) } },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Next
                    ),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(itemFocus)
                )
                Spacer(Modifier.height(4.dp))
                SizeField(
                    "Thickness", thickness, { thickness = sizeInput(it) }, thicknessUnit, { thicknessUnit = it },
                    error = if (showSizeErrors) sizeError(thickness.trim()) else null
                )
                SizeField(
                    "Width", width, { width = sizeInput(it) }, widthUnit, { widthUnit = it },
                    error = if (showSizeErrors) sizeError(width.trim()) else null
                )
                SizeField(
                    "Length", length, { length = sizeInput(it) }, lengthUnit, { lengthUnit = it },
                    error = if (showSizeErrors) sizeError(length.trim()) else null
                )
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { input -> quantity = input.filter { it.isDigit() }.take(6) },
                        label = { Text("Quantity") },
                        suffix = { Text("pcs") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.weight(1f)
                    )
                    if (isBill) {
                        OutlinedTextField(
                            value = rate,
                            onValueChange = { input ->
                                val cleaned = input.filter { it.isDigit() || it == '.' }
                                // One decimal point and at most two decimals (paise)
                                if (cleaned.count { it == '.' } <= 1 && cleaned.substringAfter('.', "").length <= 2) {
                                    rate = cleaned.take(10)
                                }
                            },
                            label = { Text("Rate per pc") },
                            prefix = { Text("₹") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                if (isBill) {
                    val amount = rate.toDoubleOrNull()?.let { it * (quantity.toIntOrNull() ?: 1) }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                            .background(Mist.copy(alpha = 0.5f), MaterialTheme.shapes.medium)
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .semantics(mergeDescendants = true) {}
                    ) {
                        Text("Amount", style = MaterialTheme.typography.bodyMedium, color = InkMuted, modifier = Modifier.weight(1f))
                        Text(
                            amount?.let(::formatRupees) ?: "Enter a rate",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (amount != null) SageInk else InkMuted
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = remarks,
                    onValueChange = { remarks = it.take(200) },
                    label = { Text("Notes") },
                    supportingText = { Text("Optional") },
                    minLines = 2,
                    maxLines = 4,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            // Actions stay above the keyboard instead of scrolling away
            HorizontalDivider(color = SandLight)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 12.dp, end = 20.dp, top = 10.dp, bottom = 12.dp)
            ) {
                if (existing != null) {
                    TextButton(onClick = { close(onDelete) }) { Text("Delete", color = ErrorRed) }
                }
                Spacer(Modifier.weight(1f))
                TextButton(onClick = { close(onDismiss) }) {
                    Text(if (existing == null && addedCount > 0) "Done" else "Cancel", color = SageDeep)
                }
                Spacer(Modifier.width(8.dp))
                Button(onClick = { save() }, shape = CircleShape, modifier = Modifier.height(48.dp)) {
                    Text(if (existing == null) "Add item" else "Save")
                }
            }
        }
    }
}

private val fractions = listOf("1/8", "1/4", "3/8", "1/2", "5/8", "3/4", "7/8")

/** Puts [fraction] after the whole number already typed: "2" + 1/8 → "2.1/8"; an earlier fraction is replaced. */
private fun withFraction(value: String, fraction: String): String {
    val whole = value.trim().takeWhile { it.isDigit() }
    return if (whole.isEmpty()) fraction else "$whole.$fraction"
}

/**
 * A size with an inch / feet switch beside it. Takes fractions like 2.1/8; while the field is
 * active, one-tap eighths sit under it because '/' is hidden on the phone keypad's second layer.
 */
@Composable
private fun SizeField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    unit: SizeUnit,
    onUnitChange: (SizeUnit) -> Unit,
    error: String? = null
) {
    var focused by remember { mutableStateOf(false) }
    // Keep the cursor where the user put it, but jump to the end when the text changes from
    // outside (a fraction tap, input clean-up or clearing after Add)
    var field by remember { mutableStateOf(TextFieldValue(value, TextRange(value.length))) }
    val shown = if (field.text == value) field else TextFieldValue(value, TextRange(value.length))
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(verticalAlignment = Alignment.Top) {
            OutlinedTextField(
                value = shown,
                onValueChange = {
                    field = it
                    onValueChange(it.text)
                },
                label = { Text(label) },
                placeholder = { Text("e.g. 2.1/8") },
                suffix = { Text(unit.short) },
                isError = error != null,
                supportingText = error?.let { { Text(it) } },
                singleLine = true,
                // The phone pad has '/', '.' and space; the decimal pad has no '/'
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .weight(1f)
                    .onFocusChanged { focused = it.isFocused }
            )
            Spacer(Modifier.width(10.dp))
            CalmSegmented(
                options = listOf("inch", "feet"),
                selectedIndex = unit.ordinal,
                onSelect = { onUnitChange(SizeUnit.entries[it]) },
                modifier = Modifier
                    .width(132.dp)
                    .padding(top = 8.dp)
            )
        }
        if (focused) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(top = 6.dp)
            ) {
                fractions.forEach { fraction ->
                    Box(
                        modifier = Modifier
                            .heightIn(min = 40.dp)
                            .clip(CircleShape)
                            .border(1.dp, Sand, CircleShape)
                            .clickable(role = Role.Button, onClickLabel = "Add $fraction") {
                                onValueChange(withFraction(value, fraction))
                            }
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(fraction, style = MaterialTheme.typography.labelLarge, color = SageInk)
                    }
                }
            }
        }
    }
}
