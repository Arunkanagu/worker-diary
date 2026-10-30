package dev.nura.app.thozhil.diary.ui

import dev.nura.app.thozhil.diary.data.Attendance
import dev.nura.app.thozhil.diary.data.AttendanceStatus
import dev.nura.app.thozhil.diary.data.Employee
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

class AttendanceReportTest {
    private val holidays = setOf("Festival / Holiday", "பண்டிகை / விடுமுறை")
    private val today = LocalDate.of(2026, 10, 30) // a Friday

    private fun present(date: LocalDate, inTime: String? = null, outTime: String? = null, ot: Double = 0.0, id: String = "1") =
        Attendance(id, date.toString(), AttendanceStatus.PRESENT, inTime, outTime, ot)

    private fun absent(date: LocalDate, reason: String = "", id: String = "1") =
        Attendance(id, date.toString(), AttendanceStatus.ABSENT, absentReason = reason)

    @Test
    fun presentAndAbsentComeStraightFromTheRecord() {
        val day = LocalDate.of(2026, 10, 28)
        assertEquals(DayMark.PRESENT, classify(present(day), day, today, holidays))
        assertEquals(DayMark.PRESENT, classify(present(day, "09:00 AM", "06:00 PM"), day, today, holidays))
        assertEquals(DayMark.ABSENT, classify(absent(day, "Sick leave"), day, today, holidays))
    }

    @Test
    fun underSixHoursIsHalfDay() {
        val day = LocalDate.of(2026, 10, 28)
        assertEquals(DayMark.HALF_DAY, classify(present(day, "09:00 AM", "01:00 PM"), day, today, holidays))
        assertEquals(DayMark.HALF_DAY, classify(present(day, "09:00 AM", "02:59 PM"), day, today, holidays))
        // Even a very short day is a half day, not absent
        assertEquals(DayMark.HALF_DAY, classify(present(day, "09:00 AM", "10:00 AM"), day, today, holidays))
        // Six hours exactly is a full day
        assertEquals(DayMark.PRESENT, classify(present(day, "09:00 AM", "03:00 PM"), day, today, holidays))
        // Older 24-hour times are read too
        assertEquals(DayMark.HALF_DAY, classify(present(day, "13:00", "18:30"), day, today, holidays))
        // Without a check-out there's nothing to measure, so it stays present
        assertEquals(DayMark.PRESENT, classify(present(day, "09:00 AM", null), day, today, holidays))
    }

    @Test
    fun holidayReasonInEitherLanguage() {
        val day = LocalDate.of(2026, 10, 28)
        assertEquals(DayMark.HOLIDAY, classify(absent(day, "Festival / Holiday"), day, today, holidays))
        assertEquals(DayMark.HOLIDAY, classify(absent(day, "பண்டிகை / விடுமுறை"), day, today, holidays))
    }

    @Test
    fun unmarkedPastSundayIsWeeklyOff() {
        val sunday = LocalDate.of(2026, 10, 25)
        assertEquals(DayOfWeek.SUNDAY, sunday.dayOfWeek)
        assertEquals(DayMark.WEEKLY_OFF, classify(null, sunday, today, holidays))
        // A Sunday that was worked is just present
        assertEquals(DayMark.PRESENT, classify(present(sunday), sunday, today, holidays))
        // Future Sundays and unmarked weekdays get nothing
        assertNull(classify(null, LocalDate.of(2026, 11, 1), today, holidays))
        assertNull(classify(null, LocalDate.of(2026, 10, 27), today, holidays))
    }

    @Test
    fun monthHasEveryDayAndWeekRunsMondayToSunday() {
        assertEquals(30, reportDays(ReportPeriod.MONTH, LocalDate.of(2026, 11, 15)).size)
        assertEquals(28, reportDays(ReportPeriod.MONTH, LocalDate.of(2026, 2, 3)).size)
        assertEquals(29, reportDays(ReportPeriod.MONTH, LocalDate.of(2028, 2, 3)).size)
        val week = reportDays(ReportPeriod.WEEK, today)
        assertEquals(7, week.size)
        assertEquals(LocalDate.of(2026, 10, 26), week.first())
        assertEquals(DayOfWeek.SUNDAY, week.last().dayOfWeek)
        // A week across a month end keeps all seven days
        assertEquals(LocalDate.of(2026, 9, 28), reportDays(ReportPeriod.WEEK, LocalDate.of(2026, 10, 2)).first())
    }

    @Test
    fun totalsCountEachMarkAndSumOvertime() {
        val week = reportDays(ReportPeriod.WEEK, today) // 26 Oct to 1 Nov
        val ravi = Employee(id = "1", name = "Ravi", mobile = "")
        val kumar = Employee(id = "2", name = "Kumar", mobile = "")
        val records = listOf(
            present(week[0], "09:00 AM", "06:00 PM", ot = 2.0),
            present(week[1], "09:00 AM", "07:30 PM", ot = 1.5),
            present(week[2], "09:00 AM", "12:00 PM"),     // half day
            absent(week[3], "Sick leave"),
            absent(week[4], "Festival / Holiday"),         // holiday
            absent(week[0], id = "2"),
            present(week[1], id = "2")
        )
        val report = buildReport(listOf(ravi, kumar), records, week, today, holidays).associateBy { it.employee.id }

        val r = report.getValue("1")
        assertEquals(2, r.present)
        assertEquals(1, r.halfDay)
        assertEquals(1, r.absent)
        assertEquals(1, r.holiday)
        assertEquals(0, r.weeklyOff) // Saturday is unmarked, Sunday is after today
        assertEquals(3.5, r.overtimeHours, 0.0001)
        assertNull(r.marks[week[5]])

        val k = report.getValue("2")
        assertEquals(1, k.present)
        assertEquals(1, k.absent)
        assertEquals(0.0, k.overtimeHours, 0.0001)
    }

    @Test
    fun overtimeOnAbsentRecordsIsIgnored() {
        val day = LocalDate.of(2026, 10, 28)
        val stray = Attendance("1", day.toString(), AttendanceStatus.ABSENT, overtimeHours = 4.0)
        val row = buildReport(listOf(Employee(id = "1", name = "A", mobile = "")), listOf(stray), listOf(day), today, holidays).single()
        assertEquals(0.0, row.overtimeHours, 0.0001)
    }
}
