package dev.nura.app.thozhil.diary.ui.screens

import androidx.compose.foundation.ScrollState
import dev.nura.app.thozhil.diary.ui.theme.Sand
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.drawBehind
import androidx.compose.material.icons.outlined.Info
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.PersonSearch
import androidx.compose.material.icons.outlined.TableChart
import androidx.compose.material.icons.outlined.ViewList
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.nura.app.thozhil.diary.R
import dev.nura.app.thozhil.diary.ui.AttendanceReportViewModel
import dev.nura.app.thozhil.diary.ui.DayMark
import dev.nura.app.thozhil.diary.ui.EmployeeReport
import dev.nura.app.thozhil.diary.ui.FULL_DAY_MIN_HOURS
import dev.nura.app.thozhil.diary.ui.ReportContent
import dev.nura.app.thozhil.diary.ui.ReportPeriod
import dev.nura.app.thozhil.diary.ui.ReportView
import dev.nura.app.thozhil.diary.ui.reportDays
import dev.nura.app.thozhil.diary.ui.components.CalmSearchField
import dev.nura.app.thozhil.diary.ui.components.CalmSegmented
import dev.nura.app.thozhil.diary.ui.components.EmptyState
import dev.nura.app.thozhil.diary.ui.components.ScreenHeader
import dev.nura.app.thozhil.diary.ui.components.SoftCard
import dev.nura.app.thozhil.diary.ui.theme.AmberInk
import dev.nura.app.thozhil.diary.ui.theme.AmberSoft
import dev.nura.app.thozhil.diary.ui.theme.ErrorRed
import dev.nura.app.thozhil.diary.ui.theme.Ink
import dev.nura.app.thozhil.diary.ui.theme.InkMuted
import dev.nura.app.thozhil.diary.ui.theme.IvoryCard
import dev.nura.app.thozhil.diary.ui.theme.Mist
import dev.nura.app.thozhil.diary.ui.theme.Moss
import dev.nura.app.thozhil.diary.ui.theme.SageDeep
import dev.nura.app.thozhil.diary.ui.theme.SageInk
import dev.nura.app.thozhil.diary.ui.theme.SandLight
import dev.nura.app.thozhil.diary.ui.theme.TableStripe
import dev.nura.app.thozhil.diary.util.Language
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle

/** Light red behind absent marks; ErrorRed on it is 5.9:1. */
private val AbsentTint = Color(0xFFFBE4E1)

private val DayMark.container: Color
    get() = when (this) {
        DayMark.PRESENT -> Mist
        DayMark.ABSENT -> AbsentTint
        DayMark.HALF_DAY -> AmberSoft
        DayMark.HOLIDAY -> IvoryCard
        DayMark.WEEKLY_OFF -> SandLight
    }

private val DayMark.content: Color
    get() = when (this) {
        DayMark.PRESENT -> SageInk
        DayMark.ABSENT -> ErrorRed
        DayMark.HALF_DAY -> AmberInk
        DayMark.HOLIDAY -> SageDeep
        DayMark.WEEKLY_OFF -> InkMuted
    }

private val DayMark.labelRes: Int
    get() = when (this) {
        DayMark.PRESENT -> R.string.present
        DayMark.ABSENT -> R.string.absent
        DayMark.HALF_DAY -> R.string.half_day
        DayMark.HOLIDAY -> R.string.holiday
        DayMark.WEEKLY_OFF -> R.string.weekly_off
    }

/** "10h", "2.5h"; blank for none so the zeros don't crowd the table. */
private fun hoursLabel(hours: Double): String = when {
    hours <= 0.0 -> "–"
    hours % 1.0 == 0.0 -> "${hours.toInt()}h"
    else -> "${"%.1f".format(hours)}h"
}

// Register sizes: big enough to read and tap at a glance, small enough that a week fits a phone
private val DayCellWidth = 40.dp
private val TotalCellWidth = 52.dp
private val RowHeight = 50.dp
private val HeaderHeight = 54.dp

/** From this width the name column gets more room and the controls sit in one row. */
private val WideMinWidth = 600.dp

@Composable
fun AttendanceReportScreen(
    onBack: () -> Unit,
    viewModel: AttendanceReportViewModel = hiltViewModel()
) {
    val period by viewModel.period.collectAsStateWithLifecycle()
    val view by viewModel.view.collectAsStateWithLifecycle()
    val anchor by viewModel.anchor.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val content by viewModel.content.collectAsStateWithLifecycle()

    val wide = LocalConfiguration.current.screenWidthDp.dp >= WideMinWidth
    var picking by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        ScreenHeader(
            title = stringResource(R.string.attendance_report),
            onBack = onBack,
            trail = listOf(stringResource(R.string.attendance)),
            modifier = Modifier.padding(start = 8.dp, end = 20.dp)
        )

        Column(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val periodTabs: @Composable (Modifier) -> Unit = { m ->
                CalmSegmented(
                    options = listOf(stringResource(R.string.monthly), stringResource(R.string.weekly)),
                    selectedIndex = period.ordinal,
                    onSelect = { viewModel.setPeriod(ReportPeriod.entries[it]) },
                    modifier = m
                )
            }
            val viewTabs: @Composable (Modifier) -> Unit = { m ->
                CalmSegmented(
                    options = listOf(stringResource(R.string.register_view), stringResource(R.string.short_view)),
                    selectedIndex = view.ordinal,
                    onSelect = { viewModel.setView(ReportView.entries[it]) },
                    // Icons only where there's room beside the labels
                    icons = if (wide) listOf(Icons.Outlined.TableChart, Icons.Outlined.ViewList) else null,
                    modifier = m
                )
            }
            if (wide) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    periodTabs(Modifier.weight(1f))
                    viewTabs(Modifier.weight(1f))
                    PeriodSwitcher(period, anchor, viewModel::step, { picking = true }, Modifier.weight(1.2f))
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    periodTabs(Modifier.weight(1f))
                    viewTabs(Modifier.weight(1f))
                }
                PeriodSwitcher(period, anchor, viewModel::step, { picking = true }, Modifier.fillMaxWidth())
            }
            CalmSearchField(
                value = query,
                onValueChange = viewModel::setQuery,
                placeholder = stringResource(R.string.search_employees)
            )
        }

        Spacer(Modifier.height(12.dp))

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (val c = content) {
                ReportContent.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = SageDeep)
                }
                is ReportContent.Error -> EmptyState(
                    icon = Icons.Outlined.ErrorOutline,
                    title = stringResource(R.string.report_could_not_load),
                    message = c.message.ifBlank { stringResource(R.string.please_try_again) }
                ) {
                    Button(onClick = viewModel::retry, shape = CircleShape) { Text(stringResource(R.string.try_again)) }
                }
                is ReportContent.Ready -> when {
                    !c.hasEmployees -> EmptyState(
                        icon = Icons.Outlined.Groups,
                        title = stringResource(R.string.no_employees_added_yet),
                        message = stringResource(R.string.add_employees_to_see_their_attendance_here)
                    )
                    !c.hasRecords -> EmptyState(
                        icon = Icons.Outlined.EventBusy,
                        title = stringResource(R.string.no_attendance_in_period),
                        message = stringResource(R.string.no_attendance_in_period_description)
                    )
                    c.rows.isEmpty() -> EmptyState(
                        icon = Icons.Outlined.PersonSearch,
                        title = stringResource(R.string.no_employees_match),
                        message = stringResource(R.string.try_another_name_or_number)
                    )
                    view == ReportView.REGISTER -> RegisterTable(c.days, c.rows, wide)
                    else -> SummaryTable(c.rows)
                }
            }
        }
    }

    if (picking) {
        when (period) {
            ReportPeriod.MONTH -> MonthPickerDialog(
                selected = YearMonth.from(anchor),
                onPick = {
                    picking = false
                    viewModel.showDate(it.atDay(1))
                },
                onDismiss = { picking = false }
            )
            ReportPeriod.WEEK -> WeekPickerDialog(
                selected = anchor,
                onPick = {
                    picking = false
                    viewModel.showDate(it)
                },
                onDismiss = { picking = false }
            )
        }
    }
}

/** Previous and next around the month or week, whose label opens a picker. */
@Composable
private fun PeriodSwitcher(
    period: ReportPeriod,
    anchor: LocalDate,
    onStep: (Int) -> Unit,
    onPick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val locale = Language.locale
    val today = LocalDate.now()
    val (title, subtitle) = when (period) {
        ReportPeriod.MONTH -> {
            val month = YearMonth.from(anchor)
            month.format(DateTimeFormatter.ofPattern("MMMM yyyy", locale)) to
                stringResource(R.string.d_days, month.lengthOfMonth())
        }
        ReportPeriod.WEEK -> {
            val days = reportDays(ReportPeriod.WEEK, anchor)
            val sameYear = days.first().year == days.last().year
            val from = days.first().format(DateTimeFormatter.ofPattern(if (sameYear) "d MMM" else "d MMM yyyy", locale))
            val to = days.last().format(DateTimeFormatter.ofPattern("d MMM yyyy", locale))
            "$from – $to" to if (today in days) stringResource(R.string.this_week) else stringResource(R.string.week)
        }
    }
    val hasNext = reportDays(period, anchor).last().isBefore(today)
    val (previousLabel, nextLabel) = when (period) {
        ReportPeriod.MONTH -> stringResource(R.string.previous_month) to stringResource(R.string.next_month)
        ReportPeriod.WEEK -> stringResource(R.string.previous_week) to stringResource(R.string.next_week)
    }

    SoftCard(modifier = modifier, shape = RoundedCornerShape(18.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 4.dp)) {
            IconButton(onClick = { onStep(-1) }) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = previousLabel, tint = Ink)
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(role = Role.Button, onClickLabel = stringResource(R.string.choose_period), onClick = onPick)
            ) {
                Icon(Icons.Outlined.CalendarMonth, contentDescription = null, tint = SageDeep, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(title, style = MaterialTheme.typography.titleSmall, color = Ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = InkMuted, maxLines = 1)
                }
            }
            IconButton(onClick = { onStep(1) }, enabled = hasNext) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = nextLabel,
                    tint = if (hasNext) Ink else InkMuted.copy(alpha = 0.35f)
                )
            }
        }
    }
}

// ---------------------------------------------------------------- Register

/**
 * The register book: one row per employee, one column per day, then the totals. Names stay put
 * while the days scroll sideways; every row shares one scroll position so the columns line up.
 */
@Composable
private fun RegisterTable(days: List<LocalDate>, rows: List<EmployeeReport>, wide: Boolean) {
    val horizontal = rememberScrollState()
    val nameWidth = if (wide) 220.dp else 128.dp
    val today = LocalDate.now()
    val dayWidthPx = with(LocalDensity.current) { DayCellWidth.roundToPx() }

    // Open on today with a couple of days before it in sight; other periods start at their first day
    LaunchedEffect(days) {
        val index = days.indexOf(today)
        horizontal.scrollTo(if (index > 2) (index - 2) * dayWidthPx else 0)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Legend(modifier = Modifier.padding(horizontal = 20.dp))
        Spacer(Modifier.height(10.dp))
        SoftCard(
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .padding(start = 20.dp, end = 20.dp, bottom = 16.dp)
                .fillMaxWidth()
                .weight(1f, fill = false)
        ) {
            Box {
                Column {
                    RegisterHeader(days, today, nameWidth, horizontal)
                    HorizontalDivider(color = SandLight)
                    LazyColumn {
                        itemsIndexed(rows, key = { _, row -> row.employee.id }) { index, row ->
                            RegisterRow(row, days, today, nameWidth, horizontal, striped = index % 2 == 1)
                            HorizontalDivider(color = SandLight)
                        }
                    }
                }
                // A fade on the end edge while more columns are out of sight, so they aren't missed
                if (horizontal.canScrollForward) {
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
    }
}

@Composable
private fun RegisterHeader(days: List<LocalDate>, today: LocalDate, nameWidth: Dp, horizontal: ScrollState) {
    Row(modifier = Modifier.height(HeaderHeight).background(SandLight.copy(alpha = 0.5f))) {
        Box(modifier = Modifier.width(nameWidth).height(HeaderHeight).padding(start = 14.dp), contentAlignment = Alignment.CenterStart) {
            Text(stringResource(R.string.employee), style = MaterialTheme.typography.labelLarge, color = InkMuted, maxLines = 1)
        }
        Row(modifier = Modifier.horizontalScroll(horizontal)) {
            days.forEach { day ->
                val isToday = day == today
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .width(DayCellWidth)
                        .height(HeaderHeight)
                        .background(if (isToday) AmberSoft else Color.Transparent)
                        .weekRule(day, days)
                ) {
                    Text(
                        "%02d".format(day.dayOfMonth),
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isToday) AmberInk else Ink,
                        fontWeight = FontWeight.SemiBold
                    )
                    // "Tue" and "Thu" rather than two Ts; shrinks for longer names in Tamil
                    BasicText(
                        day.dayOfWeek.getDisplayName(TextStyle.SHORT, Language.locale).removeSuffix("."),
                        style = MaterialTheme.typography.labelSmall.copy(color = if (isToday) AmberInk else InkMuted),
                        maxLines = 1,
                        autoSize = TextAutoSize.StepBased(minFontSize = 7.sp, maxFontSize = MaterialTheme.typography.labelSmall.fontSize),
                        modifier = Modifier.padding(horizontal = 2.dp)
                    )
                }
            }
            TotalHeader(stringResource(R.string.present_short))
            TotalHeader(stringResource(R.string.half_day_short))
            TotalHeader(stringResource(R.string.absent_short))
            TotalHeader(stringResource(R.string.ot))
        }
    }
}

@Composable
private fun TotalHeader(text: String) {
    Box(
        modifier = Modifier
            .width(TotalCellWidth)
            .height(HeaderHeight)
            .background(Mist.copy(alpha = 0.6f)),
        contentAlignment = Alignment.Center
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge, color = SageInk, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

/** A thin rule on the start edge of each Monday after the first day, so the weeks of a month read apart. */
private fun Modifier.weekRule(day: LocalDate, days: List<LocalDate>): Modifier =
    if (day.dayOfWeek != java.time.DayOfWeek.MONDAY || day == days.first()) this
    else drawBehind { drawLine(Sand, Offset(0f, 0f), Offset(0f, size.height), strokeWidth = 1.dp.toPx()) }

@Composable
private fun RegisterRow(
    row: EmployeeReport,
    days: List<LocalDate>,
    today: LocalDate,
    nameWidth: Dp,
    horizontal: ScrollState,
    striped: Boolean
) {
    val summary = rowDescription(row)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .height(RowHeight)
            .background(if (striped) TableStripe else Color.Transparent)
            .clearAndSetSemantics { contentDescription = summary }
    ) {
        Text(
            row.employee.name,
            style = MaterialTheme.typography.bodyLarge,
            color = Ink,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .width(nameWidth)
                .padding(start = 14.dp, end = 6.dp)
        )
        Row(modifier = Modifier.horizontalScroll(horizontal), verticalAlignment = Alignment.CenterVertically) {
            days.forEach { day ->
                Box(
                    modifier = Modifier
                        .width(DayCellWidth)
                        .height(RowHeight)
                        .background(if (day == today) AmberSoft.copy(alpha = 0.45f) else Color.Transparent)
                        .weekRule(day, days),
                    contentAlignment = Alignment.Center
                ) {
                    val mark = row.marks[day]
                    if (mark != null) MarkChip(mark) else Text("·", color = SandLight, style = MaterialTheme.typography.labelMedium)
                }
            }
            TotalCell("${row.present}", SageInk)
            TotalCell("${row.halfDay}", AmberInk)
            TotalCell("${row.absent}", ErrorRed)
            TotalCell(hoursLabel(row.overtimeHours), Ink)
        }
    }
}

@Composable
private fun MarkChip(mark: DayMark) {
    Box(
        modifier = Modifier
            .size(width = 34.dp, height = 30.dp)
            .background(mark.container, RoundedCornerShape(8.dp))
            .then(if (mark == DayMark.HOLIDAY) Modifier.border(1.dp, Moss, RoundedCornerShape(8.dp)) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Text(mark.code, style = MaterialTheme.typography.labelMedium, color = mark.content, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

@Composable
private fun TotalCell(text: String, color: Color) {
    Box(
        modifier = Modifier
            .width(TotalCellWidth)
            .height(RowHeight)
            .background(Mist.copy(alpha = 0.25f)),
        contentAlignment = Alignment.Center
    ) {
        Text(text, style = MaterialTheme.typography.titleMedium, color = color, fontWeight = FontWeight.SemiBold, maxLines = 1)
    }
}

/** What each mark means; the counting rules open from the info button. */
@Composable
private fun Legend(modifier: Modifier = Modifier) {
    var showRules by rememberSaveable { mutableStateOf(false) }
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f)
            ) {
                DayMark.entries.forEach { mark ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        MarkChip(mark)
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(mark.labelRes), style = MaterialTheme.typography.labelMedium, color = InkMuted)
                    }
                }
            }
            IconButton(onClick = { showRules = !showRules }) {
                Icon(
                    Icons.Outlined.Info,
                    contentDescription = stringResource(R.string.how_days_are_counted),
                    tint = if (showRules) SageDeep else InkMuted
                )
            }
        }
        AnimatedVisibility(visible = showRules) {
            Text(
                stringResource(R.string.half_day_rule_d, FULL_DAY_MIN_HOURS.toInt()),
                style = MaterialTheme.typography.bodySmall,
                color = SageInk,
                modifier = Modifier
                    .padding(top = 6.dp)
                    .fillMaxWidth()
                    .background(Mist.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }
    }
}

/** What a screen reader says for a row in either table: the name and the totals. */
@Composable
private fun rowDescription(row: EmployeeReport): String = stringResource(
    R.string.summary_row_description,
    row.employee.name, row.present, row.absent, row.halfDay, hoursLabel(row.overtimeHours)
)

// ---------------------------------------------------------------- Summary

/** Just the totals: one line per employee and a total line at the end. */
@Composable
private fun SummaryTable(rows: List<EmployeeReport>) {
    SoftCard(
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier
            .padding(start = 20.dp, end = 20.dp, bottom = 16.dp)
            .fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(HeaderHeight)
                .background(SandLight.copy(alpha = 0.5f))
                .padding(horizontal = 14.dp)
        ) {
            Text(stringResource(R.string.employee), style = MaterialTheme.typography.labelLarge, color = InkMuted, modifier = Modifier.weight(1f))
            SummaryHeader(stringResource(R.string.present_short))
            SummaryHeader(stringResource(R.string.absent_short))
            SummaryHeader(stringResource(R.string.half_day_short))
            SummaryHeader(stringResource(R.string.ot))
        }
        HorizontalDivider(color = SandLight)
        LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
            itemsIndexed(rows, key = { _, row -> row.employee.id }) { index, row ->
                val summary = rowDescription(row)
                SummaryLine(
                    name = row.employee.name,
                    present = "${row.present}",
                    absent = "${row.absent}",
                    halfDay = "${row.halfDay}",
                    ot = hoursLabel(row.overtimeHours),
                    modifier = Modifier
                        .background(if (index % 2 == 1) TableStripe else Color.Transparent)
                        .clearAndSetSemantics { contentDescription = summary }
                )
                HorizontalDivider(color = SandLight)
            }
            item {
                SummaryLine(
                    name = stringResource(R.string.total),
                    present = "${rows.sumOf { it.present }}",
                    absent = "${rows.sumOf { it.absent }}",
                    halfDay = "${rows.sumOf { it.halfDay }}",
                    ot = hoursLabel(rows.sumOf { it.overtimeHours }),
                    bold = true,
                    modifier = Modifier.background(Mist.copy(alpha = 0.5f))
                )
            }
        }
    }
}

private val SummaryCellWidth = 60.dp

@Composable
private fun SummaryHeader(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelLarge,
        color = SageInk,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        maxLines = 1,
        modifier = Modifier.width(SummaryCellWidth)
    )
}

@Composable
private fun SummaryLine(
    name: String,
    present: String,
    absent: String,
    halfDay: String,
    ot: String,
    modifier: Modifier = Modifier,
    bold: Boolean = false
) {
    val weight = if (bold) FontWeight.Bold else FontWeight.SemiBold
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 58.dp)
            .padding(horizontal = 14.dp)
    ) {
        Text(
            name,
            style = MaterialTheme.typography.bodyLarge,
            color = Ink,
            fontWeight = if (bold) FontWeight.Bold else null,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f).padding(end = 6.dp)
        )
        SummaryValue(present, SageInk, weight)
        SummaryValue(absent, ErrorRed, weight)
        SummaryValue(halfDay, AmberInk, weight)
        SummaryValue(ot, Ink, weight)
    }
}

@Composable
private fun SummaryValue(text: String, color: Color, weight: FontWeight) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        color = color,
        fontWeight = weight,
        textAlign = TextAlign.Center,
        maxLines = 1,
        modifier = Modifier.width(SummaryCellWidth)
    )
}

// ---------------------------------------------------------------- Pickers

/** A year with its twelve months; months after this one can't be picked. */
@Composable
private fun MonthPickerDialog(selected: YearMonth, onPick: (YearMonth) -> Unit, onDismiss: () -> Unit) {
    val current = YearMonth.now()
    var year by remember { mutableIntStateOf(selected.year) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = IvoryCard,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { year-- }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = stringResource(R.string.previous_year), tint = Ink)
                }
                Text("$year", style = MaterialTheme.typography.titleLarge, color = Ink, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                IconButton(onClick = { year++ }, enabled = year < current.year) {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = stringResource(R.string.next_year),
                        tint = if (year < current.year) Ink else InkMuted.copy(alpha = 0.35f)
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                (1..12).chunked(3).forEach { months ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        months.forEach { m ->
                            val month = YearMonth.of(year, m)
                            val enabled = !month.isAfter(current)
                            val isSelected = month == selected
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 44.dp)
                                    .background(if (isSelected) SageDeep else if (enabled) Mist else Color.Transparent, RoundedCornerShape(12.dp))
                                    .clickable(enabled = enabled, role = Role.Button) { onPick(month) }
                            ) {
                                Text(
                                    month.month.getDisplayName(TextStyle.SHORT, Language.locale),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = when {
                                        isSelected -> Color.White
                                        enabled -> SageInk
                                        else -> InkMuted.copy(alpha = 0.4f)
                                    },
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel), color = SageDeep) }
        }
    )
}

/** Any day up to today; the report shows the week holding it. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WeekPickerDialog(selected: LocalDate, onPick: (LocalDate) -> Unit, onDismiss: () -> Unit) {
    val todayMillis = LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    val state = rememberDatePickerState(
        initialSelectedDateMillis = selected.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis <= todayMillis
            override fun isSelectableYear(year: Int) = year <= LocalDate.now().year
        }
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        colors = DatePickerDefaults.colors(containerColor = IvoryCard),
        confirmButton = {
            Button(
                onClick = {
                    state.selectedDateMillis?.let {
                        onPick(Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate())
                    } ?: onDismiss()
                },
                shape = CircleShape
            ) { Text(stringResource(R.string.show_week)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel), color = SageDeep) }
        }
    ) {
        DatePicker(state = state, colors = DatePickerDefaults.colors(containerColor = IvoryCard))
    }
}
