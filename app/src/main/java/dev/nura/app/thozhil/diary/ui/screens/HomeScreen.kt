package dev.nura.app.thozhil.diary.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.nura.app.thozhil.diary.R
import dev.nura.app.thozhil.diary.data.SiteWithContacts
import dev.nura.app.thozhil.diary.ui.AttendanceViewModel
import dev.nura.app.thozhil.diary.ui.BillWithSite
import dev.nura.app.thozhil.diary.ui.SiteViewModel
import dev.nura.app.thozhil.diary.ui.components.AppLogo
import dev.nura.app.thozhil.diary.ui.components.AttendanceReportCard
import dev.nura.app.thozhil.diary.ui.components.ProfileAvatar
import dev.nura.app.thozhil.diary.ui.components.SoftCard
import dev.nura.app.thozhil.diary.ui.theme.Ink
import dev.nura.app.thozhil.diary.ui.theme.InkMuted
import dev.nura.app.thozhil.diary.ui.theme.Mist
import dev.nura.app.thozhil.diary.ui.theme.SageDeep
import dev.nura.app.thozhil.diary.ui.theme.SageInk
import dev.nura.app.thozhil.diary.ui.theme.SandLight
import dev.nura.app.thozhil.diary.util.dial
import dev.nura.app.thozhil.diary.util.formatRupees
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.Month
import java.time.ZoneId
import dev.nura.app.thozhil.diary.util.Language
import java.time.format.DateTimeFormatter

/** Sites and bills shown on Home before "See all". */
private const val HOME_SITE_LIMIT = 3
private const val HOME_BILL_LIMIT = 3

/** From this width Home sets Today and the work overview side by side. */
private val HomeTwoPaneMinWidth = 720.dp
/** One column stays a readable width on a large phone or a portrait tablet. */
private val HomeOneColumnMaxWidth = 640.dp
private val HomeTwoPaneMaxWidth = 1120.dp

private val billDateFormat = DateTimeFormatter.ofPattern("d MMM")

@Composable
fun HomeScreen(
    userName: String,
    photoPath: String,
    onOpenProfile: () -> Unit,
    onOpenAttendance: () -> Unit,
    onOpenSites: () -> Unit,
    onOpenSite: (String) -> Unit,
    onOpenBills: () -> Unit,
    onOpenBill: (String) -> Unit,
    attendanceViewModel: AttendanceViewModel = hiltViewModel(),
    siteViewModel: SiteViewModel = hiltViewModel()
) {
    val attendance by attendanceViewModel.dayAttendance.collectAsStateWithLifecycle()
    val month by attendanceViewModel.monthAttendance.collectAsStateWithLifecycle()
    val sites by siteViewModel.sites.collectAsStateWithLifecycle()
    val bills by siteViewModel.recentBills.collectAsStateWithLifecycle()

    @Composable
    fun Today() {
        DiaryPage(userName = userName)
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = onOpenAttendance,
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = SageDeep),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp)
        ) {
            Icon(Icons.Outlined.CalendarMonth, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.attendance), style = MaterialTheme.typography.labelLarge)
        }
        Spacer(Modifier.height(28.dp))
        SectionTitle(stringResource(R.string.todays_attendance))
        Spacer(Modifier.height(10.dp))
        AttendanceReportCard(
            attendance = attendance,
            month = month,
            actionLabel = if (attendance.any { it.status == null }) stringResource(R.string.mark_now) else stringResource(R.string.open),
            onClick = onOpenAttendance
        )
    }

    @Composable
    fun Work() {
        SectionTitle(stringResource(R.string.at_a_glance))
        Spacer(Modifier.height(10.dp))
        GlanceCard(sites = sites.size, bills = bills.size, billed = bills.sumOf { it.total })

        // Section headers are 48dp tall for their See all link, so the gaps around them are smaller
        Spacer(Modifier.height(16.dp))
        SectionHeader(
            title = stringResource(R.string.sites),
            action = if (sites.size > HOME_SITE_LIMIT) stringResource(R.string.see_all_d, sites.size) else null,
            onAction = onOpenSites
        )
        Spacer(Modifier.height(4.dp))
        SitesSheet(sites = sites.take(HOME_SITE_LIMIT), onOpenSite = onOpenSite, onAddSite = onOpenSites)

        if (sites.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            SectionHeader(
                title = stringResource(R.string.recent_bills),
                action = if (bills.isNotEmpty()) stringResource(R.string.see_all) else null,
                onAction = onOpenBills
            )
            Spacer(Modifier.height(4.dp))
            RecentBillsSheet(bills = bills.take(HOME_BILL_LIMIT), onOpenBill = onOpenBill, onOpenBills = onOpenBills)
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        val twoPane = maxWidth >= HomeTwoPaneMinWidth
        Column(
            modifier = Modifier
                .widthIn(max = if (twoPane) HomeTwoPaneMaxWidth else HomeOneColumnMaxWidth)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AppLogo(size = 40.dp)
                Spacer(Modifier.width(12.dp))
                Text(
                    stringResource(R.string.thozhil_diary),
                    style = MaterialTheme.typography.titleMedium,
                    color = Ink,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable(role = Role.Button, onClickLabel = stringResource(R.string.open_profile), onClick = onOpenProfile),
                    contentAlignment = Alignment.Center
                ) {
                    ProfileAvatar(name = userName, photoPath = photoPath, size = 40.dp)
                }
            }
            Spacer(Modifier.height(20.dp))

            if (twoPane) {
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    Column(modifier = Modifier.weight(1f)) { Today() }
                    Column(modifier = Modifier.weight(1f)) { Work() }
                }
            } else {
                Today()
                Spacer(Modifier.height(28.dp))
                Work()
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        color = Ink,
        modifier = modifier.semantics { heading() }
    )
}

/** A section title with an optional "See all" link on the right. */
@Composable
private fun SectionHeader(title: String, action: String?, onAction: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.heightIn(min = 48.dp)) {
        SectionTitle(title, modifier = Modifier.weight(1f))
        if (action != null) {
            TextButton(onClick = onAction) { Text(action, color = SageDeep) }
        }
    }
}

/** Sites, bills and what the bills add up to, side by side on one card. */
@Composable
private fun GlanceCard(sites: Int, bills: Int, billed: Double) {
    SoftCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .padding(vertical = 14.dp)
        ) {
            @Composable
            fun RowScope.Stat(value: String, label: String, weight: Float, accent: Color = Ink) {
                Column(
                    modifier = Modifier
                        .weight(weight)
                        .padding(horizontal = 16.dp)
                        .semantics(mergeDescendants = true) {}
                ) {
                    Text(value, style = MaterialTheme.typography.titleLarge, color = accent, maxLines = 1, softWrap = false)
                    Text(label, style = MaterialTheme.typography.labelMedium, color = InkMuted, maxLines = 1)
                }
            }

            @Composable
            fun Rule() {
                Box(
                    Modifier
                        .fillMaxHeight()
                        .width(1.dp)
                        .background(SandLight)
                )
            }
            Stat("$sites", if (sites == 1) stringResource(R.string.site) else stringResource(R.string.sites), weight = 1f)
            Rule()
            Stat("$bills", if (bills == 1) stringResource(R.string.bill) else stringResource(R.string.bills), weight = 1f)
            Rule()
            Stat(formatRupees(billed), stringResource(R.string.total_billed), weight = 1.8f, accent = SageDeep)
        }
    }
}

/** The newest bills on one sheet: title, site and date, and the total; tap opens the bill. */
@Composable
private fun RecentBillsSheet(bills: List<BillWithSite>, onOpenBill: (String) -> Unit, onOpenBills: () -> Unit) {
    SoftCard(modifier = Modifier.fillMaxWidth()) {
        if (bills.isEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(role = Role.Button, onClick = onOpenBills)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconWell(Icons.AutoMirrored.Outlined.ReceiptLong)
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.no_bills_yet), style = MaterialTheme.typography.titleSmall, color = Ink)
                    Text(stringResource(R.string.start_one_from_the_bills_tab), style = MaterialTheme.typography.bodyMedium, color = InkMuted)
                }
            }
            return@SoftCard
        }
        bills.forEachIndexed { index, entry ->
            if (index > 0) HorizontalDivider(color = SandLight, modifier = Modifier.padding(start = 74.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(role = Role.Button, onClickLabel = stringResource(R.string.open_s, entry.bill.title)) { onOpenBill(entry.bill.id) }
                    .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconWell(Icons.AutoMirrored.Outlined.ReceiptLong)
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        entry.bill.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = Ink,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    // The date first, so a long site name can't push it out of view
                    val date = Instant.ofEpochMilli(entry.bill.createdAt).atZone(ZoneId.systemDefault()).format(billDateFormat.withLocale(Language.locale))
                    Text(
                        "$date · ${entry.siteName}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = InkMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.width(12.dp))
                Text(formatRupees(entry.total), style = MaterialTheme.typography.titleSmall, color = SageDeep, maxLines = 1)
            }
        }
    }
}

/** A small icon on a soft green square, like the site badge. */
@Composable
private fun IconWell(icon: ImageVector) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .background(Mist, RoundedCornerShape(14.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = SageInk, modifier = Modifier.size(22.dp))
    }
}

/** A bookmark ribbon with a V-notch at the bottom. */
private val RibbonShape = GenericShape { size, _ ->
    moveTo(0f, 0f)
    lineTo(size.width, 0f)
    lineTo(size.width, size.height)
    lineTo(size.width / 2f, size.height - size.width * 0.45f)
    lineTo(0f, size.height)
    close()
}

@Composable
private fun dayOfWeekRes(dayOfWeek: DayOfWeek): Int = when (dayOfWeek) {
    DayOfWeek.MONDAY -> R.string.monday
    DayOfWeek.TUESDAY -> R.string.tuesday
    DayOfWeek.WEDNESDAY -> R.string.wednesday
    DayOfWeek.THURSDAY -> R.string.thursday
    DayOfWeek.FRIDAY -> R.string.friday
    DayOfWeek.SATURDAY -> R.string.saturday
    DayOfWeek.SUNDAY -> R.string.sunday
}

@Composable
private fun monthRes(month: Month): Int = when (month) {
    Month.JANUARY -> R.string.january
    Month.FEBRUARY -> R.string.february
    Month.MARCH -> R.string.march
    Month.APRIL -> R.string.april
    Month.MAY -> R.string.may
    Month.JUNE -> R.string.june
    Month.JULY -> R.string.july
    Month.AUGUST -> R.string.august
    Month.SEPTEMBER -> R.string.september
    Month.OCTOBER -> R.string.october
    Month.NOVEMBER -> R.string.november
    Month.DECEMBER -> R.string.december
}

/** Today's page: the date set large, a ruled line, then the greeting. A ribbon marks the page. */
@Composable
private fun DiaryPage(userName: String) {
    val today = LocalDate.now()
    val greeting = when (LocalTime.now().hour) {
        in 0..11 -> stringResource(R.string.good_morning)
        in 12..16 -> stringResource(R.string.good_afternoon)
        else -> stringResource(R.string.good_evening)
    }
    val firstName = userName.trim().substringBefore(" ").ifEmpty { stringResource(R.string.there) }

    SoftCard(modifier = Modifier.fillMaxWidth()) {
        Box {
            Column(modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 22.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clearAndSetSemantics {
                        contentDescription = today.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Language.locale))
                    }
                ) {
                    Text(
                        today.dayOfMonth.toString(),
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontSize = 76.sp,
                            lineHeight = 80.sp,
                            letterSpacing = (-3).sp,
                            fontWeight = FontWeight.Light
                        ),
                        color = SageInk
                    )
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(
                            stringResource(dayOfWeekRes(today.dayOfWeek)),
                            style = MaterialTheme.typography.titleMedium,
                            color = Ink
                        )
                        Text(
                            stringResource(monthRes(today.month)) + " " + today.year,
                            style = MaterialTheme.typography.bodyMedium,
                            color = InkMuted
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))
                HorizontalDivider(color = SandLight, thickness = 1.dp)
                Spacer(Modifier.height(14.dp))
                Text(
                    "$greeting, $firstName",
                    style = MaterialTheme.typography.bodyLarge,
                    color = InkMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 28.dp)
                    .size(width = 22.dp, height = 58.dp)
                    .background(SageDeep, RibbonShape)
            )
        }
    }
}

/** Sites on one sheet, ruled between rows, each with a call button for its first contact. */
@Composable
private fun SitesSheet(
    sites: List<SiteWithContacts>,
    onOpenSite: (String) -> Unit,
    onAddSite: () -> Unit
) {
    val context = LocalContext.current
    SoftCard(modifier = Modifier.fillMaxWidth()) {
        if (sites.isEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(role = Role.Button, onClick = onAddSite)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SiteBadge()
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.add_your_first_site), style = MaterialTheme.typography.titleSmall, color = Ink)
                    Text(
                        stringResource(R.string.save_each_building_with_the_people_to_call),
                        style = MaterialTheme.typography.bodyMedium,
                        color = InkMuted
                    )
                }
            }
            return@SoftCard
        }

        Column(verticalArrangement = Arrangement.Top) {
            sites.forEachIndexed { index, item ->
                if (index > 0) HorizontalDivider(color = SandLight, modifier = Modifier.padding(start = 78.dp))
                val contact = item.contacts.firstOrNull()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(role = Role.Button, onClickLabel = stringResource(R.string.open_s, item.site.name)) {
                            onOpenSite(item.site.id)
                        }
                        .padding(start = 16.dp, end = 6.dp, top = 12.dp, bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SiteBadge()
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            item.site.name,
                            style = MaterialTheme.typography.titleSmall,
                            color = Ink,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            contact?.name ?: stringResource(R.string.no_contacts),
                            style = MaterialTheme.typography.bodyMedium,
                            color = InkMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (contact != null && contact.mobile.isNotEmpty()) {
                        IconButton(onClick = { context.dial(contact.mobile) }) {
                            Icon(Icons.Outlined.Call, contentDescription = stringResource(R.string.call_s, contact.name), tint = SageDeep)
                        }
                    }
                }
            }
        }
    }
}
