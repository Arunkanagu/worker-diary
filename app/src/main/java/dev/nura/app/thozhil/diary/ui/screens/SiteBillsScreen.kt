package dev.nura.app.thozhil.diary.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.nura.app.thozhil.diary.data.SiteNote
import dev.nura.app.thozhil.diary.ui.SiteViewModel
import dev.nura.app.thozhil.diary.ui.components.ClearFocusWhenClosed
import dev.nura.app.thozhil.diary.ui.components.ConfirmDialog
import dev.nura.app.thozhil.diary.ui.components.EmptyState
import dev.nura.app.thozhil.diary.ui.components.ScreenHeader
import dev.nura.app.thozhil.diary.util.formatRupees

/** One site's bills, newest first, each with its total. */
@Composable
fun SiteBillsScreen(
    siteId: Long,
    onBack: () -> Unit,
    onOpenBill: (Long) -> Unit,
    viewModel: SiteViewModel = hiltViewModel()
) {
    val site by remember(siteId) { viewModel.observe(siteId) }.collectAsStateWithLifecycle(initialValue = null)
    val bills by remember(siteId) { viewModel.observeBills(siteId) }.collectAsStateWithLifecycle(initialValue = emptyList())
    val totals by remember(siteId) { viewModel.observeBillTotals(siteId) }.collectAsStateWithLifecycle(initialValue = emptyMap())
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
            .statusBarsPadding()
    ) {
        LazyColumn(
            contentPadding = PaddingValues(bottom = 120.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                ScreenHeader(
                    title = site?.site?.name.orEmpty(),
                    subtitle = when (bills.size) {
                        0 -> "Bills"
                        1 -> "1 bill, ${formatRupees(grandTotal)}"
                        else -> "${bills.size} bills, ${formatRupees(grandTotal)}"
                    },
                    onBack = onBack,
                    modifier = Modifier.padding(start = 8.dp, end = 20.dp)
                )
                Spacer(Modifier.height(8.dp))
            }

            if (bills.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.AutoMirrored.Outlined.ReceiptLong,
                        title = "No bills yet",
                        message = "Tap New bill to start one. Add items with a rate and the amounts add up for you.",
                        modifier = Modifier.padding(top = 48.dp)
                    )
                }
            } else {
                itemsIndexed(bills, key = { _, bill -> bill.id }) { index, bill ->
                    NoteRow(
                        note = bill,
                        first = index == 0,
                        last = index == bills.lastIndex,
                        onOpen = { onOpenBill(bill.id) },
                        onEditTitle = { openSheet(bill) },
                        onDelete = { deleting = bill },
                        trailing = formatRupees(totals[bill.id] ?: 0.0)
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
            Text("New bill", style = MaterialTheme.typography.labelLarge)
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
            title = "Delete this bill?",
            message = "\"${bill.title.take(60)}\" and its items will be removed. This can't be undone.",
            confirmLabel = "Delete",
            onConfirm = {
                viewModel.deleteNote(bill)
                deleting = null
            },
            onDismiss = { deleting = null }
        )
    }
}
