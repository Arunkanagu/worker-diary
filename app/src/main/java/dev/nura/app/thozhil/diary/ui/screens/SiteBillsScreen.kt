package dev.nura.app.thozhil.diary.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.nura.app.thozhil.diary.R
import dev.nura.app.thozhil.diary.data.SiteNote
import dev.nura.app.thozhil.diary.ui.SiteViewModel
import dev.nura.app.thozhil.diary.ui.components.ClearFocusWhenClosed
import dev.nura.app.thozhil.diary.ui.components.ConfirmDialog
import dev.nura.app.thozhil.diary.ui.components.ContentMaxWidth
import dev.nura.app.thozhil.diary.ui.components.EmptyState
import dev.nura.app.thozhil.diary.ui.components.ScreenHeader
import dev.nura.app.thozhil.diary.util.formatRupees

/** One site's bills, newest first, each with its total. */
@Composable
fun SiteBillsScreen(
    siteId: String,
    onBack: () -> Unit,
    onOpenBill: (String) -> Unit,
    viewModel: SiteViewModel = hiltViewModel()
) {
    val site by remember(siteId) { viewModel.observe(siteId) }.collectAsStateWithLifecycle(initialValue = null)
    val bills by remember(siteId) { viewModel.observeBills(siteId) }.collectAsStateWithLifecycle(initialValue = emptyList())
    val totals by viewModel.billTotals.collectAsStateWithLifecycle(initialValue = emptyMap())
    val counts by remember(siteId) { viewModel.observeNoteCounts(siteId) }.collectAsStateWithLifecycle(initialValue = emptyMap())
    var sheetOpen by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<SiteNote?>(null) }
    var deleting by remember { mutableStateOf<SiteNote?>(null) }
    val grandTotal = bills.sumOf { totals[it.id] ?: 0.0 }

    fun openSheet(bill: SiteNote?) {
        editing = bill
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
                title = site?.site?.name.orEmpty(),
                subtitle = when (bills.size) {
                    0 -> stringResource(R.string.bills)
                    1 -> stringResource(R.string.one_bill_s, formatRupees(grandTotal))
                    else -> stringResource(R.string.d_bills_s, bills.size, formatRupees(grandTotal))
                },
                onBack = onBack,
                trail = listOf(stringResource(R.string.bills)),
                modifier = Modifier.padding(start = 8.dp, end = 20.dp)
            )

            if (bills.isEmpty()) {
                EmptyState(
                    icon = Icons.AutoMirrored.Outlined.ReceiptLong,
                    title = stringResource(R.string.no_bills_yet),
                    message = stringResource(R.string.tap_new_bill_description),
                    modifier = Modifier.padding(top = 24.dp)
                )
            } else {
                // One column on a phone, two or three on a tablet
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(NoteCardMinWidth),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 120.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(bills, key = { it.id }) { bill ->
                        NoteCard(
                            note = bill,
                            counts = counts[bill.id],
                            total = totals[bill.id] ?: 0.0,
                            onOpen = { onOpenBill(bill.id) },
                            onEditTitle = { openSheet(bill) },
                            onDelete = { deleting = bill }
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
            Text(stringResource(R.string.new_bill), style = MaterialTheme.typography.labelLarge)
        }
    }

    ClearFocusWhenClosed(open = sheetOpen || deleting != null)

    if (sheetOpen) {
        NoteSheet(
            existing = editing,
            isBill = true,
            onDismiss = { sheetOpen = false },
            onSave = { title ->
                viewModel.saveNote(editing?.copy(title = title) ?: SiteNote(siteId = siteId, title = title, isBill = true))
                sheetOpen = false
            },
            onDelete = {
                deleting = editing
                sheetOpen = false
            }
        )
    }

    deleting?.let { bill ->
        ConfirmDialog(
            title = stringResource(R.string.delete_this_bill_question),
            message = stringResource(R.string.bill_delete_warning, bill.title.take(60)),
            confirmLabel = stringResource(R.string.delete),
            onConfirm = {
                viewModel.deleteNote(bill)
                deleting = null
            },
            onDismiss = { deleting = null }
        )
    }
}
