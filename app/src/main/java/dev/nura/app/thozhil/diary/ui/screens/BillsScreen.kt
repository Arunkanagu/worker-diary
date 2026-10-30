package dev.nura.app.thozhil.diary.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.nura.app.thozhil.diary.R
import dev.nura.app.thozhil.diary.ui.SiteViewModel
import dev.nura.app.thozhil.diary.ui.components.EmptyState
import dev.nura.app.thozhil.diary.ui.components.ScreenHeader
import dev.nura.app.thozhil.diary.ui.components.SoftCard
import dev.nura.app.thozhil.diary.ui.theme.Ink
import dev.nura.app.thozhil.diary.ui.theme.InkMuted
import dev.nura.app.thozhil.diary.ui.theme.SageDeep
import dev.nura.app.thozhil.diary.util.formatRupees

/** Bills tab: each site with how many bills it has and their total; tap a site to see its bills. */
@Composable
fun BillsScreen(
    onOpenSiteBills: (String) -> Unit,
    viewModel: SiteViewModel = hiltViewModel()
) {
    val sites by viewModel.sites.collectAsStateWithLifecycle()
    val summaries by viewModel.billSummaries.collectAsStateWithLifecycle()
    val grandTotal = summaries.values.sumOf { it.total ?: 0.0 }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        ScreenHeader(
            title = stringResource(R.string.bills),
            subtitle = if (summaries.isEmpty()) stringResource(R.string.pick_a_site_to_start_a_bill) else stringResource(R.string.all_sites_s, formatRupees(grandTotal)),
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        if (sites.isEmpty()) {
            EmptyState(
                icon = Icons.AutoMirrored.Outlined.ReceiptLong,
                title = stringResource(R.string.no_sites_yet),
                message = stringResource(R.string.add_a_site_in_the_sites_tab_first_its_bills_will_be_kept_here)
            )
            return@Column
        }

        LazyColumn(
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(sites, key = { it.site.id }) { item ->
                val summary = summaries[item.site.id]
                SoftCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .clickable(role = Role.Button, onClickLabel = stringResource(R.string.open_bills_for_s, item.site.name)) {
                                onOpenSiteBills(item.site.id)
                            }
                            .padding(start = 16.dp, end = 10.dp, top = 14.dp, bottom = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SiteBadge()
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                item.site.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = Ink,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                when (summary?.bills ?: 0) {
                                    0 -> stringResource(R.string.no_bills_yet)
                                    1 -> stringResource(R.string.one_bill)
                                    else -> stringResource(R.string.d_bills, summary?.bills ?: 0)
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = InkMuted
                            )
                        }
                        if (summary != null) {
                            Text(
                                formatRupees(summary.total ?: 0.0),
                                style = MaterialTheme.typography.titleSmall,
                                color = SageDeep
                            )
                        }
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = InkMuted,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
