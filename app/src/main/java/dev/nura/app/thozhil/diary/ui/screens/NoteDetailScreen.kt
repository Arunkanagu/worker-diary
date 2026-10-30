package dev.nura.app.thozhil.diary.ui.screens

import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.nura.app.thozhil.diary.R
import dev.nura.app.thozhil.diary.data.AdjustmentType
import dev.nura.app.thozhil.diary.data.BillAdjustment
import dev.nura.app.thozhil.diary.data.BillTotals
import dev.nura.app.thozhil.diary.data.NoteItem
import dev.nura.app.thozhil.diary.data.NoteTable
import dev.nura.app.thozhil.diary.data.SiteNote
import dev.nura.app.thozhil.diary.data.SizeUnit
import dev.nura.app.thozhil.diary.data.amount
import dev.nura.app.thozhil.diary.data.areaSqFt
import dev.nura.app.thozhil.diary.data.formatSize
import dev.nura.app.thozhil.diary.ui.NoteViewModel
import dev.nura.app.thozhil.diary.ui.TableWithItems
import dev.nura.app.thozhil.diary.ui.components.CalmSegmented
import dev.nura.app.thozhil.diary.ui.components.ClearFocusWhenClosed
import dev.nura.app.thozhil.diary.ui.components.ConfirmDialog
import dev.nura.app.thozhil.diary.ui.components.EmptyState
import dev.nura.app.thozhil.diary.ui.components.ScreenHeader
import dev.nura.app.thozhil.diary.ui.components.SoftCard
import dev.nura.app.thozhil.diary.ui.components.StatusPill
import dev.nura.app.thozhil.diary.ui.components.WideLayoutMinWidth
import dev.nura.app.thozhil.diary.ui.theme.CoverMint
import dev.nura.app.thozhil.diary.ui.theme.ErrorRed
import dev.nura.app.thozhil.diary.ui.theme.Ink
import dev.nura.app.thozhil.diary.ui.theme.InkMuted
import dev.nura.app.thozhil.diary.ui.theme.IvoryCard
import dev.nura.app.thozhil.diary.ui.theme.Mist
import dev.nura.app.thozhil.diary.ui.theme.SageDeep
import dev.nura.app.thozhil.diary.ui.theme.SageInk
import dev.nura.app.thozhil.diary.ui.theme.SandLight
import dev.nura.app.thozhil.diary.ui.theme.TableStripe
import dev.nura.app.thozhil.diary.util.NotePdf
import dev.nura.app.thozhil.diary.util.SharedPrefHelper
import dev.nura.app.thozhil.diary.util.formatRupees
import dev.nura.app.thozhil.diary.util.formatSqFt
import dev.nura.app.thozhil.diary.util.plainNumber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Table columns, in order, with fixed widths so rows line up while scrolling sideways. The number
 * and item stay put at the start, like names in the attendance register; the rest scroll.
 */
private enum class TableColumn(@param:StringRes val labelRes: Int, val width: Dp, val numeric: Boolean = false) {
    NUMBER(R.string.number_col, 40.dp, numeric = true),
    ITEM(R.string.item, 128.dp),
    WIDTH(R.string.width, 88.dp, numeric = true),
    LENGTH(R.string.length, 88.dp, numeric = true),
    THICKNESS(R.string.thickness, 104.dp, numeric = true),
    QUANTITY(R.string.qty, 68.dp, numeric = true),
    RATE(R.string.rate_title, 100.dp, numeric = true),
    AMOUNT(R.string.amount, 120.dp, numeric = true),
    NOTES(R.string.note, 180.dp)
}

/** The columns that stay in view while the others scroll. */
private val FixedColumns = setOf(TableColumn.NUMBER, TableColumn.ITEM)

// Table sizes, matching the attendance report's register
private val TableHeaderHeight = 52.dp
private val TableRowMinHeight = 56.dp
private val CellPadding = 10.dp

@Composable
private fun itemCount(count: Int) = if (count == 1) stringResource(R.string.one_item) else stringResource(R.string.d_items, count)

/** The item or adjustment form beside the tables on a landscape tablet. */
private val ItemPanelWidth = 440.dp
private val PanelShape = RoundedCornerShape(topStart = 28.dp, bottomStart = 28.dp)

/** What the side panel shows; it keeps showing the last one while it slides away. */
private sealed interface PanelEntry {
    data class Item(val table: NoteTable, val editing: NoteItem?) : PanelEntry
    data class Adjustment(val editing: BillAdjustment?) : PanelEntry
}

/** Bills add Rate and Amount; notes leave them out. */
private fun columnsFor(isBill: Boolean) =
    if (isBill) TableColumn.entries else TableColumn.entries - setOf(TableColumn.RATE, TableColumn.AMOUNT)

@Composable
fun NoteDetailScreen(
    onBack: () -> Unit,
    onOpenPdf: (path: String, noteId: String) -> Unit,
    viewModel: NoteViewModel = hiltViewModel()
) {
    val note by viewModel.note.collectAsStateWithLifecycle()
    val site by viewModel.site.collectAsStateWithLifecycle()
    val tables by viewModel.tables.collectAsStateWithLifecycle()
    val adjustments by viewModel.adjustments.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Which table the item sheet adds to, and the row being edited (null when adding)
    var itemSheetTable by remember { mutableStateOf<NoteTable?>(null) }
    var editingItem by remember { mutableStateOf<NoteItem?>(null) }
    var deletingItem by remember { mutableStateOf<NoteItem?>(null) }
    var tableSheetOpen by remember { mutableStateOf(false) }
    var editingTable by remember { mutableStateOf<NoteTable?>(null) }
    var deletingTable by remember { mutableStateOf<TableWithItems?>(null) }
    // Advance / discount / tax: the sheet is open, the entry being edited (null when adding)
    var adjustmentSheetOpen by remember { mutableStateOf(false) }
    var editingAdjustment by remember { mutableStateOf<BillAdjustment?>(null) }
    var deletingAdjustment by remember { mutableStateOf<BillAdjustment?>(null) }
    var exporting by remember { mutableStateOf(false) }

    // On a landscape tablet the item and adjustment forms sit beside the tables instead of covering them
    val configuration = LocalConfiguration.current
    val sidePanel = configuration.screenWidthDp.dp >= WideLayoutMinWidth &&
        configuration.screenWidthDp > configuration.screenHeightDp
    var panelEntry by remember { mutableStateOf<PanelEntry?>(null) }
    LaunchedEffect(itemSheetTable, editingItem, adjustmentSheetOpen, editingAdjustment) {
        val table = itemSheetTable
        when {
            table != null -> panelEntry = PanelEntry.Item(table, editingItem)
            adjustmentSheetOpen -> panelEntry = PanelEntry.Adjustment(editingAdjustment)
        }
    }

    val isBill = note?.isBill == true
    val allItems = tables.flatMap { it.items }
    val totalPieces = allItems.sumOf { it.quantity ?: 0 }
    val totalAmount = allItems.sumOf { it.amount() ?: 0.0 }
    // Hidden tables and adjustments don't count, on screen as in the PDF; hiding or showing one
    // updates these straight away
    val anyHidden = tables.any { it.table.hideInPdf } || adjustments.any { it.hideInPdf }
    val billTotals = NotePdf.pdfTotals(tables, adjustments)

    fun openItemSheet(table: NoteTable, item: NoteItem?) {
        adjustmentSheetOpen = false
        editingItem = item
        itemSheetTable = table
    }

    fun openAdjustmentSheet(adjustment: BillAdjustment?) {
        itemSheetTable = null
        editingAdjustment = adjustment
        adjustmentSheetOpen = true
    }

    fun openTableSheet(table: NoteTable?) {
        editingTable = table
        tableSheetOpen = true
    }

    val exportPdfErrorPrefix = stringResource(R.string.couldnt_make_the_pdf_s, "")

    fun exportPdf() {
        val current = note ?: return
        scope.launch {
            exporting = true
            try {
                val style = SharedPrefHelper(context).getPdfStyle()
                val file = withContext(Dispatchers.IO) { NotePdf.write(context, site, current, tables, adjustments, style, viewModel.preparedBy.value) }
                // Preview first; the style can be changed and the PDF shared or downloaded from there
                onOpenPdf(file.absolutePath, current.id)
            } catch (e: Exception) {
                Toast.makeText(context, exportPdfErrorPrefix + e.message.orEmpty(), Toast.LENGTH_LONG).show()
            } finally {
                exporting = false
            }
        }
    }

    @Composable
    fun ItemEditor(table: NoteTable, editing: NoteItem?, asPanel: Boolean) {
        ItemSheet(
            tableName = table.name,
            isBill = isBill,
            existing = editing,
            onDismiss = { itemSheetTable = null },
            onSave = {
                viewModel.saveItem(it.copy(tableId = table.id))
                // Adding keeps the form open for the next row; editing closes it
                if (editing != null) itemSheetTable = null
            },
            onDelete = {
                deletingItem = editing
                itemSheetTable = null
            },
            asPanel = asPanel
        )
    }

    @Composable
    fun AdjustmentEditor(editing: BillAdjustment?, asPanel: Boolean) {
        // What the bill comes to without the entry being edited
        val others = NotePdf.pdfTotals(tables, adjustments.filter { it.id != editing?.id })
        AdjustmentSheet(
            existing = editing,
            maxDiscount = others.billTotal.coerceAtLeast(0.0),
            onDismiss = { adjustmentSheetOpen = false },
            onSave = {
                viewModel.saveAdjustment(it)
                adjustmentSheetOpen = false
            },
            asPanel = asPanel
        )
    }

    Row(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
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
                        isBill && (adjustments.isNotEmpty() || anyHidden) -> "${site?.site?.name.orEmpty()}, ${formatRupees(billTotals.billTotal)}"
                        tables.isEmpty() -> site?.site?.name
                        allItems.isEmpty() -> stringResource(R.string.s_no_items_yet, site?.site?.name.orEmpty())
                        isBill -> "${site?.site?.name.orEmpty()}, ${itemCount(allItems.size)}, ${formatRupees(totalAmount)}"
                        else -> stringResource(R.string.s_s_d_pcs, site?.site?.name.orEmpty(), itemCount(allItems.size), totalPieces)
                    },
                    onBack = onBack,
                    trail = listOf(if (isBill) stringResource(R.string.bills) else stringResource(R.string.sites), site?.site?.name.orEmpty()),
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
                            Icon(Icons.Outlined.PictureAsPdf, contentDescription = stringResource(R.string.preview_pdf), tint = SageDeep)
                        }
                    }
                }

                if (tables.isEmpty()) {
                    EmptyState(
                        icon = Icons.Outlined.TableChart,
                        title = stringResource(R.string.no_tables_yet),
                        message = if (isBill) {
                            stringResource(R.string.tap_new_table_doors_example)
                        } else {
                            stringResource(R.string.tap_new_table_windows_example)
                        },
                        modifier = Modifier.padding(top = 48.dp)
                    )
                }

                tables.forEach { entry ->
                    TableSection(
                        entry = entry,
                        isBill = isBill,
                        onAddItem = { openItemSheet(entry.table, null) },
                        onEditItem = { openItemSheet(entry.table, it) },
                        onRename = { openTableSheet(entry.table) },
                        onToggleHide = { viewModel.saveTable(entry.table.copy(hideInPdf = !entry.table.hideInPdf)) },
                        onDelete = { deletingTable = entry }
                    )
                }

                if (isBill) {
                    AdjustmentsSection(
                        adjustments = adjustments,
                        onEdit = { openAdjustmentSheet(it) },
                        onToggleHide = { viewModel.saveAdjustment(it.copy(hideInPdf = !it.hideInPdf)) },
                        onDelete = { deletingAdjustment = it }
                    )
                    BillSummaryCard(
                        totals = billTotals,
                        showSignature = note?.showSignature != false,
                        anyHidden = anyHidden,
                        onToggleSignature = { isChecked -> viewModel.setShowSignature(isChecked) }
                    )
                }
            }

            // Standard bottom actions
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
            ) {
                if (isBill) {
                    // Small floating button for discount / tax / advance
                    Box(
                        modifier = Modifier
                            .heightIn(min = 56.dp)
                            .shadow(10.dp, CircleShape)
                            .clip(CircleShape)
                            .background(IvoryCard)
                            .clickable(role = Role.Button, onClickLabel = stringResource(R.string.add_advance_discount_or_tax)) { openAdjustmentSheet(null) }
                            .padding(horizontal = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Add, contentDescription = null, tint = Ink)
                            Spacer(Modifier.width(6.dp))
                            Text(stringResource(R.string.add), style = MaterialTheme.typography.labelLarge, color = Ink, maxLines = 1, softWrap = false)
                        }
                    }
                }
                Button(
                    onClick = { openTableSheet(null) },
                    shape = CircleShape,
                    modifier = Modifier
                        .heightIn(min = 56.dp)
                        .shadow(10.dp, CircleShape)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.new_table), style = MaterialTheme.typography.labelLarge, maxLines = 1, softWrap = false)
                }
            }
        }

        if (sidePanel) {
            val panelVisible = itemSheetTable != null || adjustmentSheetOpen
            AnimatedVisibility(
                visible = panelVisible,
                enter = slideInHorizontally { it },
                exit = slideOutHorizontally { it }
            ) {
                Box(
                    modifier = Modifier
                        .width(ItemPanelWidth)
                        .fillMaxHeight()
                        .shadow(16.dp, PanelShape)
                        .clip(PanelShape)
                        .background(IvoryCard)
                        .border(BorderStroke(1.dp, SandLight), PanelShape)
                ) {
                    when (val current = panelEntry) {
                        is PanelEntry.Item -> ItemEditor(current.table, current.editing, asPanel = true)
                        is PanelEntry.Adjustment -> AdjustmentEditor(current.editing, asPanel = true)
                        null -> Unit
                    }
                }
            }
        }
    }

    if (!sidePanel && itemSheetTable != null) {
        ItemEditor(itemSheetTable!!, editingItem, asPanel = false)
    }

    if (!sidePanel && adjustmentSheetOpen) {
        AdjustmentEditor(editingAdjustment, asPanel = false)
    }

    if (tableSheetOpen) {
        TableNameSheet(
            existing = editingTable,
            suggestedName = stringResource(R.string.table_d, tables.size + 1),
            onDismiss = { tableSheetOpen = false },
            onSave = { name ->
                viewModel.saveTable(editingTable?.copy(name = name) ?: NoteTable(noteId = "", name = name))
                tableSheetOpen = false
            }
        )
    }

    ClearFocusWhenClosed(open = itemSheetTable != null || tableSheetOpen || deletingItem != null || deletingTable != null || adjustmentSheetOpen || deletingAdjustment != null)

    deletingItem?.let { item ->
        ConfirmDialog(
            title = stringResource(R.string.delete_s, item.item),
            message = stringResource(R.string.row_removed_from_table_warning),
            confirmLabel = stringResource(R.string.delete),
            onConfirm = {
                viewModel.deleteItem(item)
                deletingItem = null
            },
            onDismiss = { deletingItem = null }
        )
    }

    deletingAdjustment?.let { adjustment ->
        val adjTypeName = stringResource(adjustment.type.labelRes).lowercase()
        ConfirmDialog(
            title = stringResource(R.string.delete_s_of_s, adjTypeName, formatRupees(adjustment.amount)),
            message = stringResource(R.string.removed_from_bill_totals_change_warning),
            confirmLabel = stringResource(R.string.delete),
            onConfirm = {
                viewModel.deleteAdjustment(adjustment)
                deletingAdjustment = null
            },
            onDismiss = { deletingAdjustment = null }
        )
    }

    deletingTable?.let { entry ->
        ConfirmDialog(
            title = stringResource(R.string.delete_s, entry.table.name),
            message = when (entry.items.size) {
                0 -> stringResource(R.string.empty_table_removed_warning)
                1 -> stringResource(R.string.table_and_1_item_removed_warning)
                else -> stringResource(R.string.table_and_d_items_removed_warning, entry.items.size)
            },
            confirmLabel = stringResource(R.string.delete),
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
    onEditItem: (NoteItem) -> Unit,
    onRename: () -> Unit,
    onToggleHide: () -> Unit,
    onDelete: () -> Unit
) {
    val table = entry.table
    val items = entry.items
    val pieces = items.sumOf { it.quantity ?: 0 }
    val columns = columnsFor(isBill)
    var menuOpen by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(top = 16.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    table.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (table.hideInPdf) InkMuted else Ink,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    if (items.isEmpty()) stringResource(R.string.no_items) else stringResource(R.string.s_d_pcs, itemCount(items.size), pieces),
                    style = MaterialTheme.typography.bodySmall,
                    color = InkMuted
                )
            }
            if (table.hideInPdf) {
                StatusPill(stringResource(R.string.hidden), container = Mist, content = InkMuted)
                Spacer(Modifier.width(8.dp))
            }
            Box {
                IconButton(onClick = { menuOpen = true }) {
                    Icon(Icons.Filled.MoreHoriz, contentDescription = stringResource(R.string.options_for_s, table.name), tint = Ink)
                }
                DropdownMenu(
                    expanded = menuOpen,
                    onDismissRequest = { menuOpen = false },
                    containerColor = IvoryCard
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.rename)) },
                        leadingIcon = { Icon(Icons.Outlined.Edit, contentDescription = null) },
                        onClick = {
                            menuOpen = false
                            onRename()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(if (table.hideInPdf) stringResource(R.string.show) else stringResource(R.string.hide)) },
                        leadingIcon = { Icon(Icons.Outlined.PictureAsPdf, contentDescription = null) },
                        onClick = {
                            menuOpen = false
                            onToggleHide()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.delete_table), color = ErrorRed) },
                        leadingIcon = { Icon(Icons.Outlined.DeleteOutline, contentDescription = null, tint = ErrorRed) },
                        onClick = {
                            menuOpen = false
                            onDelete()
                        }
                    )
                }
            }
        }

        SoftCard(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth()
        ) {
            // A hidden table folds down to its name and options; Show from the menu opens it again
            if (!table.hideInPdf) {
                TableGrid(
                    items = items,
                    columns = columns,
                    onRowClick = onEditItem
                )
            }
        }

        if (!table.hideInPdf) {
            OutlinedButton(
                onClick = onAddItem,
                shape = CircleShape,
                border = BorderStroke(1.dp, SageDeep),
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, tint = SageDeep)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.add_item_to_s, table.name), color = SageDeep, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

/** Advances, discounts and taxes on a bill. */
@Composable
private fun AdjustmentsSection(
    adjustments: List<BillAdjustment>,
    onEdit: (BillAdjustment) -> Unit,
    onToggleHide: (BillAdjustment) -> Unit,
    onDelete: (BillAdjustment) -> Unit
) {
    if (adjustments.isEmpty()) return
    Column(modifier = Modifier.padding(top = 20.dp, start = 20.dp, end = 20.dp)) {
        Text(
            stringResource(R.string.adjustments),
            style = MaterialTheme.typography.titleMedium,
            color = Ink,
            modifier = Modifier.semantics { heading() }
        )
        Spacer(Modifier.height(8.dp))
        SoftCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                adjustments.forEachIndexed { index, adjustment ->
                    if (index > 0) HorizontalDivider(color = SandLight, modifier = Modifier.padding(horizontal = 16.dp))
                    AdjustmentRow(
                        adjustment = adjustment,
                        onEdit = { onEdit(adjustment) },
                        onToggleHide = { onToggleHide(adjustment) },
                        onDelete = { onDelete(adjustment) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AdjustmentRow(
    adjustment: BillAdjustment,
    onEdit: () -> Unit,
    onToggleHide: () -> Unit,
    onDelete: () -> Unit
) {
    val adjName = stringResource(adjustment.type.labelRes)
    val name = "$adjName ${formatRupees(adjustment.amount)}"
    val editDesc = stringResource(R.string.edit_s, name)
    val deleteDesc = stringResource(R.string.delete_s, name)
    val togglePdfDesc = if (adjustment.hideInPdf) stringResource(R.string.show_s_in_pdf, name) else stringResource(R.string.hide_s_in_pdf, name)
    val toggleText = if (adjustment.hideInPdf) stringResource(R.string.show) else stringResource(R.string.hide)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 6.dp, top = 10.dp, bottom = 10.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(adjName, style = MaterialTheme.typography.titleSmall, color = Ink)
                if (adjustment.hideInPdf) {
                    Spacer(Modifier.width(8.dp))
                    StatusPill(stringResource(R.string.hidden), container = Mist, content = InkMuted)
                }
            }
            Text(
                when (adjustment.type) {
                    AdjustmentType.ADVANCE -> stringResource(R.string.already_paid_lowers_balance)
                    AdjustmentType.DISCOUNT -> stringResource(R.string.taken_off_bill_total)
                    AdjustmentType.TAX -> stringResource(R.string.added_to_bill_total)
                },
                style = MaterialTheme.typography.bodySmall,
                color = InkMuted
            )
        }
        Text(
            formatRupees(adjustment.amount),
            style = MaterialTheme.typography.titleSmall,
            color = Ink
        )
        Spacer(Modifier.width(4.dp))
        IconButton(onClick = onEdit) {
            Icon(Icons.Outlined.Edit, contentDescription = editDesc, tint = SageDeep, modifier = Modifier.size(20.dp))
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Outlined.DeleteOutline, contentDescription = deleteDesc, tint = ErrorRed, modifier = Modifier.size(20.dp))
        }
        TextButton(onClick = onToggleHide) {
            Text(
                toggleText,
                color = InkMuted,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.semantics { contentDescription = togglePdfDesc }
            )
        }
    }
}

/** Bill summary: subtotal, discount, tax, total, advance and balance due. */
@Composable
private fun BillSummaryCard(
    totals: BillTotals,
    showSignature: Boolean,
    anyHidden: Boolean,
    onToggleSignature: (Boolean) -> Unit
) {
    val adjusted = totals.discount != 0.0 || totals.tax != 0.0
    Column(modifier = Modifier.padding(top = 16.dp, start = 20.dp, end = 20.dp)) {
        SoftCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                @Composable
                fun line(label: String, value: String, strong: Boolean = false) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                    ) {
                        Text(
                            label,
                            style = if (strong) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
                            color = if (strong) Ink else InkMuted,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            value,
                            style = if (strong) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
                            color = if (strong) Ink else InkMuted
                        )
                    }
                }

                if (adjusted || totals.advance != 0.0) line(stringResource(R.string.items_total), formatRupees(totals.subtotal))
                if (totals.discount != 0.0) line(stringResource(R.string.discount), "− ${formatRupees(totals.discount)}")
                if (totals.tax != 0.0) line(stringResource(R.string.tax), "+ ${formatRupees(totals.tax)}")
                line(stringResource(R.string.bill_total), formatRupees(totals.billTotal), strong = true)
                if (totals.advance != 0.0) {
                    line(stringResource(R.string.advance), "− ${formatRupees(totals.advance)}")
                    line(stringResource(R.string.balance_due), formatRupees(totals.balanceDue), strong = true)
                }

                if (anyHidden) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.hidden_tables_not_counted),
                        style = MaterialTheme.typography.labelSmall,
                        color = InkMuted
                    )
                }

                HorizontalDivider(color = SandLight, modifier = Modifier.padding(vertical = 12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.signature_in_pdf), style = MaterialTheme.typography.bodyMedium, color = Ink)
                        Text(
                            if (showSignature) stringResource(R.string.a_line_to_sign_on) else stringResource(R.string.left_off_the_pdf),
                            style = MaterialTheme.typography.bodySmall,
                            color = InkMuted
                        )
                    }
                    Switch(
                        checked = showSignature,
                        onCheckedChange = onToggleSignature,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = SageDeep,
                            uncheckedThumbColor = InkMuted,
                            uncheckedTrackColor = SandLight
                        )
                    )
                }
            }
        }
    }
}

/** Form sheet for an advance, discount or tax. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdjustmentSheet(
    existing: BillAdjustment?,
    maxDiscount: Double,
    onDismiss: () -> Unit,
    onSave: (BillAdjustment) -> Unit,
    asPanel: Boolean = false
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var type by remember { mutableStateOf(existing?.type ?: AdjustmentType.ADVANCE) }
    var amountText by remember { mutableStateOf(existing?.amount?.let { plainNumber(it) }.orEmpty()) }
    var amountError by remember { mutableStateOf<String?>(null) }
    val focus = remember { FocusRequester() }

    val amountAboveZeroMsg = stringResource(R.string.enter_amount_above_zero)
    val discountTooHighMsg = stringResource(R.string.discount_cant_be_more_than_bill, formatRupees(maxDiscount))

    fun save() {
        val value = amountText.toDoubleOrNull()
        amountError = when {
            value == null || value <= 0.0 -> amountAboveZeroMsg
            type == AdjustmentType.DISCOUNT && value > maxDiscount -> discountTooHighMsg
            else -> null
        }
        if (amountError == null && value != null) {
            val saved = (existing ?: BillAdjustment(noteId = "", type = type, amount = value)).copy(type = type, amount = value)
            if (asPanel) onSave(saved) else scope.launch { sheetState.hide() }.invokeOnCompletion { onSave(saved) }
        }
    }

    LaunchedEffect(Unit) { focus.requestFocus() }

    @Composable
    fun Content() {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = if (asPanel) 24.dp else 0.dp, bottom = 16.dp)
        ) {
            Text(
                if (existing == null) stringResource(R.string.add_adjustment) else stringResource(R.string.edit_adjustment),
                style = MaterialTheme.typography.titleLarge,
                color = Ink
            )
            Spacer(Modifier.height(16.dp))

            CalmSegmented(
                options = AdjustmentType.entries.map { stringResource(it.labelRes) },
                selectedIndex = type.ordinal,
                onSelect = {
                    type = AdjustmentType.entries[it]
                    amountError = null
                }
            )

            Spacer(Modifier.height(8.dp))
            Text(
                when (type) {
                    AdjustmentType.ADVANCE -> stringResource(R.string.already_paid_lowers_balance)
                    AdjustmentType.DISCOUNT -> stringResource(R.string.taken_off_bill_total)
                    AdjustmentType.TAX -> stringResource(R.string.added_to_bill_total)
                },
                style = MaterialTheme.typography.bodySmall,
                color = InkMuted
            )

            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = amountText,
                onValueChange = { input ->
                    amountText = input.filter { it.isDigit() || it == '.' }.take(10)
                    amountError = null
                },
                label = { Text(stringResource(R.string.amount)) },
                prefix = { Text("₹ ") },
                isError = amountError != null,
                supportingText = amountError?.let { { Text(it) } },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { save() }),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focus)
            )

            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(Modifier.weight(1f))
                TextButton(onClick = { if (asPanel) onDismiss() else scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() } }) {
                    Text(stringResource(R.string.cancel), color = SageDeep)
                }
                Spacer(Modifier.width(8.dp))
                Button(onClick = { save() }, shape = CircleShape, modifier = Modifier.heightIn(min = 48.dp)) {
                    Text(stringResource(R.string.save))
                }
            }
        }
    }

    if (asPanel) {
        Content()
    } else {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            containerColor = IvoryCard
        ) { Content() }
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
                if (existing == null) stringResource(R.string.new_table) else stringResource(R.string.rename_table),
                style = MaterialTheme.typography.titleLarge,
                color = Ink
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it.take(60) },
                label = { Text(stringResource(R.string.table_name)) },
                placeholder = { Text(suggestedName) },
                supportingText = { Text(stringResource(R.string.eg_ground_floor_windows)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { save() }),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focus)
            )
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(Modifier.weight(1f))
                TextButton(onClick = { close(onDismiss) }) { Text(stringResource(R.string.cancel), color = SageDeep) }
                Spacer(Modifier.width(8.dp))
                Button(onClick = { save() }, shape = CircleShape, modifier = Modifier.heightIn(min = 48.dp)) {
                    Text(if (existing == null) stringResource(R.string.create_table) else stringResource(R.string.save))
                }
            }
        }
    }
}

/** Sanitize dimensions like "2", "2.5" or "2 1/2" into clean feet and inches for table cells. */
private fun sizeInput(text: String): String {
    val clean = text.filter { it.isDigit() || it == '.' || it == ' ' || it == '/' }
    return clean
}

/** The table grid, with fixed-width headers and data cells, scrolling sideways as a unit. */
@Composable
private fun TableGrid(
    items: List<NoteItem>,
    columns: List<TableColumn>,
    onRowClick: (NoteItem) -> Unit
) {
    val hScroll = rememberScrollState()
    val totalPieces = items.sumOf { it.quantity ?: 0 }
    val totalAmount = items.sumOf { it.amount() ?: 0.0 }
    val fixed = columns.filter { it in FixedColumns }
    val scrolling = columns - FixedColumns

    // Each line is the fixed number and item, then the rest in a strip that every line scrolls together
    @Composable
    fun Line(modifier: Modifier, fixedCell: @Composable (TableColumn) -> Unit, cell: @Composable (TableColumn) -> Unit) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier.fillMaxWidth()) {
            fixed.forEach { col -> Box(Modifier.width(col.width).padding(start = if (col == TableColumn.NUMBER) 8.dp else 0.dp)) { fixedCell(col) } }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.horizontalScroll(hScroll)) {
                scrolling.forEach { col -> Box(Modifier.width(col.width).padding(horizontal = CellPadding)) { cell(col) } }
            }
        }
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Line(
                modifier = Modifier
                    .heightIn(min = TableHeaderHeight)
                    .background(SandLight.copy(alpha = 0.5f)),
                fixedCell = { col -> HeaderLabel(stringResource(col.labelRes), col) },
                cell = { col -> HeaderLabel(stringResource(col.labelRes), col) }
            )
            HorizontalDivider(color = SandLight)

            items.forEachIndexed { index, item ->
                val rowClickLabel = stringResource(R.string.change_s, item.item)
                Line(
                    modifier = Modifier
                        .heightIn(min = TableRowMinHeight)
                        .background(if (index % 2 == 1) TableStripe else Color.Transparent)
                        .clickable(role = Role.Button, onClickLabel = rowClickLabel) { onRowClick(item) },
                    fixedCell = { col ->
                        if (col == TableColumn.NUMBER) RowNumber(index + 1)
                        else Text(
                            item.item.ifEmpty { "–" },
                            style = MaterialTheme.typography.bodyLarge,
                            color = Ink,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(start = 10.dp, end = 6.dp, top = 8.dp, bottom = 8.dp)
                        )
                    },
                    cell = { col ->
                        val value = when (col) {
                            TableColumn.WIDTH -> formatSize(item.width, item.widthUnit)
                            TableColumn.LENGTH -> formatSize(item.length, item.lengthUnit)
                            TableColumn.THICKNESS -> formatSize(item.thickness, item.thicknessUnit)
                            TableColumn.QUANTITY -> item.quantity?.toString().orEmpty()
                            TableColumn.RATE -> item.rate?.let { formatRupees(it) }.orEmpty()
                            TableColumn.AMOUNT -> item.amount()?.let { formatRupees(it) }.orEmpty()
                            TableColumn.NOTES -> item.remarks
                            else -> ""
                        }
                        Text(
                            value.ifEmpty { "–" },
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (col == TableColumn.AMOUNT) SageInk else if (value.isEmpty()) InkMuted else Ink,
                            fontWeight = if (col == TableColumn.AMOUNT) FontWeight.SemiBold else null,
                            textAlign = if (col.numeric) TextAlign.End else TextAlign.Start,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                )
                HorizontalDivider(color = SandLight)
            }

            if (items.isNotEmpty()) {
                Line(
                    modifier = Modifier
                        .heightIn(min = TableRowMinHeight)
                        .background(Mist.copy(alpha = 0.6f)),
                    fixedCell = { col ->
                        if (col == TableColumn.ITEM) Text(
                            stringResource(R.string.total),
                            style = MaterialTheme.typography.titleMedium,
                            color = SageInk,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 10.dp)
                        )
                    },
                    cell = { col ->
                        val value = when (col) {
                            TableColumn.QUANTITY -> "$totalPieces"
                            TableColumn.AMOUNT -> formatRupees(totalAmount)
                            else -> ""
                        }
                        Text(
                            value,
                            style = MaterialTheme.typography.titleMedium,
                            color = SageInk,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.End,
                            maxLines = 1,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                )
            }
        }
        // A fade on the end edge while more columns are out of sight, as in the attendance register
        if (hScroll.canScrollForward) {
            Box(Modifier.matchParentSize()) {
                Box(
                    Modifier
                        .align(Alignment.CenterEnd)
                        .fillMaxHeight()
                        .width(28.dp)
                        .background(Brush.horizontalGradient(listOf(IvoryCard.copy(alpha = 0f), IvoryCard)))
                )
            }
        }
    }
}

@Composable
private fun HeaderLabel(text: String, col: TableColumn) {
    Text(
        text,
        style = MaterialTheme.typography.labelLarge,
        color = InkMuted,
        textAlign = when {
            col == TableColumn.NUMBER -> TextAlign.Center
            col.numeric -> TextAlign.End
            else -> TextAlign.Start
        },
        maxLines = 1,
        modifier = when (col) {
            // Centred over the number badges below it
            TableColumn.NUMBER -> Modifier.width(28.dp)
            TableColumn.ITEM -> Modifier.fillMaxWidth().padding(start = 10.dp)
            else -> Modifier.fillMaxWidth()
        }
    )
}

/** The row's number in a small badge, so it never runs into the item name beside it. */
@Composable
private fun RowNumber(number: Int) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(28.dp)
            .background(Mist, CircleShape)
    ) {
        Text("$number", style = MaterialTheme.typography.labelMedium, color = SageInk, fontWeight = FontWeight.SemiBold, maxLines = 1)
    }
}

/** Form sheet for adding or editing an item inside a table. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ItemSheet(
    tableName: String,
    isBill: Boolean,
    existing: NoteItem?,
    onDismiss: () -> Unit,
    onSave: (NoteItem) -> Unit,
    onDelete: () -> Unit,
    asPanel: Boolean = false
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var itemText by remember { mutableStateOf(existing?.item.orEmpty()) }
    var width by remember { mutableStateOf(existing?.width.orEmpty()) }
    var widthUnit by remember { mutableStateOf(existing?.widthUnit ?: SizeUnit.INCH) }
    var length by remember { mutableStateOf(existing?.length.orEmpty()) }
    var lengthUnit by remember { mutableStateOf(existing?.lengthUnit ?: SizeUnit.FEET) }
    var thickness by remember { mutableStateOf(existing?.thickness.orEmpty()) }
    var thicknessUnit by remember { mutableStateOf(existing?.thicknessUnit ?: SizeUnit.INCH) }
    var quantityText by remember { mutableStateOf(existing?.quantity?.toString().orEmpty()) }
    var rateText by remember { mutableStateOf(existing?.rate?.let { plainNumber(it) }.orEmpty()) }
    var remarksText by remember { mutableStateOf(existing?.remarks.orEmpty()) }

    var itemError by remember { mutableStateOf<String?>(null) }
    var rateError by remember { mutableStateOf<String?>(null) }
    var addedCount by remember { mutableStateOf(0) }
    val focus = remember { FocusRequester() }

    val itemRequiredMsg = stringResource(R.string.please_enter_the_item)
    val rateInvalidMsg = stringResource(R.string.use_a_rate_like)

    fun save() {
        val name = itemText.trim()
        itemError = if (name.isEmpty()) itemRequiredMsg else null
        val rate = rateText.trim()
        rateError = if (isBill && rate.isNotEmpty() && rate.toDoubleOrNull() == null) rateInvalidMsg else null

        if (itemError == null && rateError == null) {
            val saved = (existing ?: NoteItem(noteId = "", item = name)).copy(
                item = name,
                width = width,
                widthUnit = widthUnit,
                length = length,
                lengthUnit = lengthUnit,
                thickness = thickness,
                thicknessUnit = thicknessUnit,
                quantity = quantityText.toIntOrNull() ?: 1,
                rate = if (isBill) rate.toDoubleOrNull() else null,
                remarks = remarksText.trim()
            )
            onSave(saved)
            addedCount++
            // Clear fields so another row can be added straight away
            itemText = ""
            width = ""
            length = ""
            thickness = ""
            quantityText = ""
            rateText = ""
            remarksText = ""
        }
    }

    LaunchedEffect(Unit) { focus.requestFocus() }

    @Composable
    fun Content() {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = if (asPanel) 24.dp else 0.dp, bottom = 16.dp)
        ) {
            Text(
                if (existing == null) stringResource(R.string.new_item) else stringResource(R.string.edit_item),
                style = MaterialTheme.typography.titleLarge,
                color = Ink
            )
            Text(tableName, style = MaterialTheme.typography.bodyMedium, color = InkMuted)
            if (existing == null && addedCount > 0) {
                Spacer(Modifier.height(6.dp))
                StatusPill(
                    if (addedCount == 1) stringResource(R.string.added_s, itemText) else stringResource(R.string.s_d_added_so_far, itemText, addedCount),
                    container = CoverMint.copy(alpha = 0.25f),
                    content = SageInk
                )
            }
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = itemText,
                onValueChange = {
                    itemText = it
                    itemError = null
                },
                label = { Text(stringResource(R.string.item_name)) },
                isError = itemError != null,
                supportingText = itemError?.let { { Text(it) } },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                ),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focus)
            )

            Spacer(Modifier.height(12.dp))
            DimensionField(stringResource(R.string.width), width, { width = sizeInput(it) }, widthUnit, { widthUnit = it })
            Spacer(Modifier.height(8.dp))
            DimensionField(stringResource(R.string.length), length, { length = sizeInput(it) }, lengthUnit, { lengthUnit = it })
            Spacer(Modifier.height(8.dp))
            DimensionField(stringResource(R.string.thickness), thickness, { thickness = sizeInput(it) }, thicknessUnit, { thicknessUnit = it })

            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { input -> quantityText = input.filter { it.isDigit() }.take(5) },
                    label = { Text(stringResource(R.string.quantity)) },
                    suffix = { Text(stringResource(R.string.pcs)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.weight(1f)
                )
                if (isBill) {
                    OutlinedTextField(
                        value = rateText,
                        onValueChange = { input ->
                            rateText = input.filter { it.isDigit() || it == '.' }.take(10)
                            rateError = null
                        },
                        // Without both sizes a row is priced per piece, so the rate is too
                        label = {
                            val bySqFt = areaSqFt(width, widthUnit, length, lengthUnit) != null
                            Text(stringResource(if (bySqFt) R.string.rate_per_sq_ft else R.string.rate_per_piece))
                        },
                        prefix = { Text("₹ ") },
                        isError = rateError != null,
                        supportingText = rateError?.let { { Text(it) } },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (isBill) {
                val area = NoteItem(noteId = "", item = "", width = width, widthUnit = widthUnit, length = length, lengthUnit = lengthUnit).areaSqFt()
                val pieces = quantityText.toIntOrNull()
                val rateValue = rateText.toDoubleOrNull()
                val amount = if (rateValue != null) (area ?: 1.0) * (pieces ?: 1) * rateValue else null
                Spacer(Modifier.height(10.dp))
                SoftCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(stringResource(R.string.amount), style = MaterialTheme.typography.bodyMedium, color = InkMuted, modifier = Modifier.weight(1f))
                            Text(
                                amount?.let(::formatRupees) ?: stringResource(R.string.enter_a_rate),
                                style = MaterialTheme.typography.titleMedium,
                                color = if (amount != null) SageDeep else InkMuted
                            )
                        }
                        Text(
                            when {
                                area != null -> stringResource(R.string.s_sq_ft_d_pcs_s, formatSqFt(area), pieces ?: 1, rateValue?.let(::formatRupees) ?: stringResource(R.string.rate))
                                rateValue != null -> stringResource(R.string.d_pcs_s_each, pieces ?: 1, formatRupees(rateValue))
                                else -> stringResource(R.string.enter_width_length_to_price)
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = InkMuted
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = remarksText,
                onValueChange = { remarksText = it },
                label = { Text(stringResource(R.string.note)) },
                supportingText = { Text(stringResource(R.string.optional)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { save() }),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (existing != null) {
                    TextButton(onClick = { if (asPanel) onDelete() else scope.launch { sheetState.hide() }.invokeOnCompletion { onDelete() } }) {
                        Text(stringResource(R.string.delete), color = ErrorRed)
                    }
                }
                Spacer(Modifier.weight(1f))
                TextButton(onClick = { if (asPanel) onDismiss() else scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() } }) {
                    Text(if (existing == null && addedCount > 0) stringResource(R.string.done) else stringResource(R.string.cancel), color = SageDeep)
                }
                Spacer(Modifier.width(8.dp))
                Button(onClick = { save() }, shape = CircleShape, modifier = Modifier.heightIn(min = 48.dp)) {
                    Text(if (existing == null) stringResource(R.string.add_item) else stringResource(R.string.save))
                }
            }
        }
    }

    if (asPanel) {
        Content()
    } else {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            containerColor = IvoryCard
        ) { Content() }
    }
}

/** Field for a dimension with a unit toggle beside it (in / ft). */
@Composable
private fun DimensionField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    unit: SizeUnit,
    onUnitChange: (SizeUnit) -> Unit
) {
    val units = SizeUnit.entries
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.weight(1f)
        )
        Row(
            modifier = Modifier
                .height(56.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(SandLight)
                .padding(3.dp)
        ) {
            units.forEach { fraction ->
                val selected = fraction == unit
                val bg = if (selected) SageDeep else Color.Transparent
                val fg = if (selected) Color.White else InkMuted
                val fractionUnitText = if (fraction == SizeUnit.INCH) stringResource(R.string.inch) else stringResource(R.string.feet)
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(bg)
                        .clickable(role = Role.Button, onClickLabel = stringResource(R.string.add_s, fractionUnitText)) { onUnitChange(fraction) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(fraction.short, style = MaterialTheme.typography.labelMedium, color = fg)
                }
            }
        }
    }
}
