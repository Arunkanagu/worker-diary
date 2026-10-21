package dev.nura.app.thozhil.diary.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Call
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
import androidx.compose.ui.platform.LocalContext
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
import dev.nura.app.thozhil.diary.data.SiteWithContacts
import dev.nura.app.thozhil.diary.ui.AttendanceViewModel
import dev.nura.app.thozhil.diary.ui.SiteViewModel
import dev.nura.app.thozhil.diary.ui.components.AppLogo
import dev.nura.app.thozhil.diary.ui.components.AttendanceReportCard
import dev.nura.app.thozhil.diary.ui.components.InitialsAvatar
import dev.nura.app.thozhil.diary.ui.components.SoftCard
import dev.nura.app.thozhil.diary.ui.theme.Ink
import dev.nura.app.thozhil.diary.ui.theme.InkMuted
import dev.nura.app.thozhil.diary.ui.theme.SageDeep
import dev.nura.app.thozhil.diary.ui.theme.SageInk
import dev.nura.app.thozhil.diary.ui.theme.SandLight
import dev.nura.app.thozhil.diary.util.dial
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/** Sites shown on Home before "See all". */
private const val HOME_SITE_LIMIT = 3

@Composable
fun HomeScreen(
    userName: String,
    onOpenProfile: () -> Unit,
    onOpenAttendance: () -> Unit,
    onOpenSites: () -> Unit,
    onOpenSite: (Long) -> Unit,
    attendanceViewModel: AttendanceViewModel = hiltViewModel(),
    siteViewModel: SiteViewModel = hiltViewModel()
) {
    val attendance by attendanceViewModel.dayAttendance.collectAsStateWithLifecycle()
    val month by attendanceViewModel.monthAttendance.collectAsStateWithLifecycle()
    val sites by siteViewModel.sites.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AppLogo(size = 40.dp)
            Spacer(Modifier.width(12.dp))
            Text(
                "Thozhil Diary",
                style = MaterialTheme.typography.titleMedium,
                color = Ink,
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .clickable(role = Role.Button, onClickLabel = "Open profile", onClick = onOpenProfile),
                contentAlignment = Alignment.Center
            ) {
                InitialsAvatar(name = userName, size = 40.dp)
            }
        }

        Spacer(Modifier.height(20.dp))
        DiaryPage(userName = userName)

        Spacer(Modifier.height(28.dp))
        SectionTitle("Today's attendance")
        Spacer(Modifier.height(10.dp))
        AttendanceReportCard(
            attendance = attendance,
            month = month,
            actionLabel = if (attendance.any { it.status == null }) "Mark now" else "Open",
            onClick = onOpenAttendance
        )

        Spacer(Modifier.height(28.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            SectionTitle("Sites", modifier = Modifier.weight(1f))
            if (sites.size > HOME_SITE_LIMIT) {
                TextButton(onClick = onOpenSites) {
                    Text("See all ${sites.size}", color = SageDeep)
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        SitesSheet(sites = sites.take(HOME_SITE_LIMIT), onOpenSite = onOpenSite, onAddSite = onOpenSites)
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

/** A bookmark ribbon with a V-notch at the bottom. */
private val RibbonShape = GenericShape { size, _ ->
    moveTo(0f, 0f)
    lineTo(size.width, 0f)
    lineTo(size.width, size.height)
    lineTo(size.width / 2f, size.height - size.width * 0.45f)
    lineTo(0f, size.height)
    close()
}

/** Today's page: the date set large, a ruled line, then the greeting. A ribbon marks the page. */
@Composable
private fun DiaryPage(userName: String) {
    val today = LocalDate.now()
    val greeting = when (LocalTime.now().hour) {
        in 0..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        else -> "Good evening"
    }
    val firstName = userName.trim().substringBefore(" ").ifEmpty { "there" }

    SoftCard(modifier = Modifier.fillMaxWidth()) {
        Box {
            Column(modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 22.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clearAndSetSemantics {
                        contentDescription = today.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy"))
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
                            today.format(DateTimeFormatter.ofPattern("EEEE")),
                            style = MaterialTheme.typography.titleMedium,
                            color = Ink
                        )
                        Text(
                            today.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
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
    onOpenSite: (Long) -> Unit,
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
                    Text("Add your first site", style = MaterialTheme.typography.titleSmall, color = Ink)
                    Text(
                        "Save each building with the people to call",
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
                        .clickable(role = Role.Button, onClickLabel = "Open ${item.site.name}") {
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
                            contact?.name ?: "No contacts",
                            style = MaterialTheme.typography.bodyMedium,
                            color = InkMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (contact != null && contact.mobile.isNotEmpty()) {
                        IconButton(onClick = { context.dial(contact.mobile) }) {
                            Icon(Icons.Outlined.Call, contentDescription = "Call ${contact.name}", tint = SageDeep)
                        }
                    }
                }
            }
        }
    }
}
