package dev.nura.app.thozhil.diary.ui

import android.content.Context
import android.content.res.Configuration
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.nura.app.thozhil.diary.R
import dev.nura.app.thozhil.diary.data.AppDatabase
import dev.nura.app.thozhil.diary.data.Attendance
import dev.nura.app.thozhil.diary.data.AttendanceStatus
import dev.nura.app.thozhil.diary.data.Employee
import dev.nura.app.thozhil.diary.util.AppLanguage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import javax.inject.Inject

enum class ReportPeriod { MONTH, WEEK }

enum class ReportView { REGISTER, SUMMARY }

/**
 * What a register cell shows for a day. Attendance is only ever saved as present or absent, so the
 * rest is read from what was saved: see [classify].
 */
enum class DayMark(val code: String) {
    PRESENT("P"),
    ABSENT("A"),
    HALF_DAY("HD"),
    HOLIDAY("H"),
    WEEKLY_OFF("WO")
}

/** One employee's line in the report, with the period's totals. */
data class EmployeeReport(
    val employee: Employee,
    val marks: Map<LocalDate, DayMark>,
    val present: Int,
    val absent: Int,
    val halfDay: Int,
    val holiday: Int,
    val weeklyOff: Int,
    val overtimeHours: Double
)

sealed interface ReportContent {
    data object Loading : ReportContent
    data class Error(val message: String) : ReportContent
    data class Ready(
        val days: List<LocalDate>,
        val rows: List<EmployeeReport>,
        val hasEmployees: Boolean,
        /** Whether anything at all was marked in the period, before the search is applied. */
        val hasRecords: Boolean
    ) : ReportContent
}

/** A full day is at least this long from check-in to check-out; anything shorter is a half day. */
const val FULL_DAY_MIN_HOURS = 6L

/** Weekly off: a Sunday with nothing marked. */
private val WeeklyOffDay = DayOfWeek.SUNDAY

/** Times are saved as "08:30 AM"; older records may be "08:30". */
private val timeFormats = listOf(
    DateTimeFormatter.ofPattern("hh:mm a"),
    DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH),
    DateTimeFormatter.ofPattern("HH:mm")
)

private fun parseTime(text: String?): LocalTime? {
    if (text.isNullOrBlank()) return null
    return timeFormats.firstNotNullOfOrNull { runCatching { LocalTime.parse(text.trim(), it) }.getOrNull() }
}

/**
 * The mark for one day:
 * - present, but out less than [FULL_DAY_MIN_HOURS] after in: half day (without both times, present)
 * - absent with the festival / holiday reason: holiday
 * - nothing marked on a past Sunday: weekly off
 * Days after [today] and other unmarked days have no mark.
 */
fun classify(record: Attendance?, date: LocalDate, today: LocalDate, holidayReasons: Set<String>): DayMark? = when {
    record == null -> if (date.dayOfWeek == WeeklyOffDay && !date.isAfter(today)) DayMark.WEEKLY_OFF else null
    record.status == AttendanceStatus.ABSENT ->
        if (record.absentReason.trim() in holidayReasons) DayMark.HOLIDAY else DayMark.ABSENT
    else -> {
        val inTime = parseTime(record.inTime)
        val outTime = parseTime(record.outTime)
        val worked = if (inTime != null && outTime != null && outTime.isAfter(inTime)) Duration.between(inTime, outTime) else null
        if (worked != null && worked.toMinutes() < FULL_DAY_MIN_HOURS * 60) DayMark.HALF_DAY else DayMark.PRESENT
    }
}

/** The days a report covers: the whole month, or Monday to Sunday of the week holding [anchor]. */
fun reportDays(period: ReportPeriod, anchor: LocalDate): List<LocalDate> = when (period) {
    ReportPeriod.MONTH -> YearMonth.from(anchor).let { m -> (1..m.lengthOfMonth()).map { m.atDay(it) } }
    ReportPeriod.WEEK -> anchor.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).let { start -> (0L..6L).map { start.plusDays(it) } }
}

fun buildReport(
    employees: List<Employee>,
    records: List<Attendance>,
    days: List<LocalDate>,
    today: LocalDate,
    holidayReasons: Set<String>
): List<EmployeeReport> {
    val byEmployee = records.groupBy { it.employeeId }
    return employees.map { employee ->
        val byDate = byEmployee[employee.id].orEmpty().associateBy { it.date }
        val marks = buildMap {
            days.forEach { day ->
                classify(byDate[day.toString()], day, today, holidayReasons)?.let { put(day, it) }
            }
        }
        EmployeeReport(
            employee = employee,
            marks = marks,
            present = marks.values.count { it == DayMark.PRESENT },
            absent = marks.values.count { it == DayMark.ABSENT },
            halfDay = marks.values.count { it == DayMark.HALF_DAY },
            holiday = marks.values.count { it == DayMark.HOLIDAY },
            weeklyOff = marks.values.count { it == DayMark.WEEKLY_OFF },
            // records only hold the report's days, so every present record counts
            overtimeHours = byDate.values.filter { it.status == AttendanceStatus.PRESENT }.sumOf { it.overtimeHours }
        )
    }
}

/**
 * The attendance report: a month or a week, as a register or a short summary. The period, view,
 * date and search live here, so switching one keeps the others.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AttendanceReportViewModel @Inject constructor(
    private val db: AppDatabase,
    private val saved: SavedStateHandle,
    @ApplicationContext context: Context
) : ViewModel() {

    /** The holiday reason as saved, in every language the app has, since it's saved as shown. */
    private val holidayReasons: Set<String> = AppLanguage.entries.map { language ->
        val config = Configuration(context.resources.configuration).apply { setLocale(Locale.forLanguageTag(language.code)) }
        context.createConfigurationContext(config).getString(R.string.festival_holiday).trim()
    }.toSet()

    // Kept in the saved state, so the choices outlast the screen being rebuilt or the app being stopped
    val period: StateFlow<ReportPeriod> = saved.getStateFlow(KEY_PERIOD, ReportPeriod.MONTH)
    val view: StateFlow<ReportView> = saved.getStateFlow(KEY_VIEW, ReportView.REGISTER)

    /** Any day in the period being shown. */
    val anchor: StateFlow<LocalDate> = saved.getStateFlow(KEY_ANCHOR, LocalDate.now())
    val query: StateFlow<String> = saved.getStateFlow(KEY_QUERY, "")

    private val reload = MutableStateFlow(0)

    val content: StateFlow<ReportContent> = combine(period, anchor, reload) { period, anchor, _ -> reportDays(period, anchor) }
        .flatMapLatest { days ->
            combine(
                db.employeeDao().observeAll(),
                db.attendanceDao().observeBetween(days.first().toString(), days.last().toString()),
                query
            ) { employees, records, query ->
                val today = LocalDate.now()
                val q = query.trim()
                val matching = if (q.isEmpty()) employees else employees.filter {
                    it.name.contains(q, ignoreCase = true) || it.mobile.contains(q)
                }
                ReportContent.Ready(
                    days = days,
                    rows = buildReport(matching, records, days, today, holidayReasons),
                    hasEmployees = employees.isNotEmpty(),
                    hasRecords = records.isNotEmpty()
                ) as ReportContent
            }
                .onStart { emit(ReportContent.Loading) }
                .catch { emit(ReportContent.Error(it.message.orEmpty())) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ReportContent.Loading)

    fun setPeriod(period: ReportPeriod) {
        saved[KEY_PERIOD] = period
    }

    fun setView(view: ReportView) {
        saved[KEY_VIEW] = view
    }

    fun setQuery(query: String) {
        saved[KEY_QUERY] = query
    }

    /** Shows the period holding [date]; nothing after the current period. */
    fun showDate(date: LocalDate) {
        val today = LocalDate.now()
        saved[KEY_ANCHOR] = if (date.isAfter(today)) today else date
    }

    /** One month or week back (-1) or forward (+1). */
    fun step(direction: Int) {
        val current = anchor.value
        val next = when (period.value) {
            // The first of the month, so a short month never pushes the date along
            ReportPeriod.MONTH -> YearMonth.from(current).plusMonths(direction.toLong()).atDay(1)
            ReportPeriod.WEEK -> current.plusWeeks(direction.toLong())
        }
        // The current period is the last one
        if (direction > 0 && reportDays(period.value, next).first().isAfter(LocalDate.now())) return
        showDate(next)
    }

    fun retry() {
        reload.value++
    }

    private companion object {
        const val KEY_PERIOD = "period"
        const val KEY_VIEW = "view"
        const val KEY_ANCHOR = "anchor"
        const val KEY_QUERY = "query"
    }
}
