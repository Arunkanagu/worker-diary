package dev.nura.app.thozhil.diary.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Handyman
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.nura.app.thozhil.diary.R
import dev.nura.app.thozhil.diary.ui.components.CalmSegmented
import dev.nura.app.thozhil.diary.ui.components.ConfirmDialog
import dev.nura.app.thozhil.diary.ui.components.ProfileAvatar
import dev.nura.app.thozhil.diary.ui.components.ScreenHeader
import dev.nura.app.thozhil.diary.ui.components.SoftCard
import dev.nura.app.thozhil.diary.ui.theme.ErrorRed
import dev.nura.app.thozhil.diary.ui.theme.Ink
import dev.nura.app.thozhil.diary.ui.theme.InkMuted
import dev.nura.app.thozhil.diary.ui.theme.Mist
import dev.nura.app.thozhil.diary.ui.theme.SageInk
import dev.nura.app.thozhil.diary.ui.theme.SandLight
import dev.nura.app.thozhil.diary.util.AppLanguage
import dev.nura.app.thozhil.diary.util.Language
import dev.nura.app.thozhil.diary.data.User
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/** From this width the sections sit in two columns. */
private val TwoColumnMinWidth = 600.dp

@Composable
fun ProfileScreen(
    profile: User,
    startedOn: LocalDate,
    onEdit: () -> Unit,
    onLogout: () -> Unit,
    onLanguageChange: (AppLanguage) -> Unit,
    onBack: (() -> Unit)? = null
) {
    var confirmLogout by remember { mutableStateOf(false) }
    val twoColumns = LocalConfiguration.current.screenWidthDp.dp >= TwoColumnMinWidth
    val notAdded = stringResource(R.string.not_added)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ScreenHeader(
            title = stringResource(R.string.profile),
            onBack = onBack,
            // A tab has no back arrow, so the title lines up with the content
            modifier = Modifier.padding(start = if (onBack == null) 20.dp else 8.dp, end = 12.dp)
        ) {
            IconButton(onClick = onEdit) {
                Icon(Icons.Outlined.Edit, contentDescription = stringResource(R.string.edit_profile), tint = Ink)
            }
        }

        Spacer(Modifier.height(8.dp))

        Column(
            modifier = Modifier
                .widthIn(max = 960.dp)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            IdentityCard(profile, startedOn, onEdit)

            val personal: @Composable () -> Unit = {
                ProfileSection(stringResource(R.string.personal_information)) {
                    InfoRow(Icons.Outlined.Person, stringResource(R.string.full_name), profile.name.ifBlank { notAdded })
                }
            }
            val contact: @Composable () -> Unit = {
                ProfileSection(stringResource(R.string.contact_information)) {
                    InfoRow(Icons.Outlined.Phone, stringResource(R.string.mobile_number), profile.mobile.ifBlank { notAdded })
                    InfoDivider()
                    InfoRow(Icons.Outlined.Email, stringResource(R.string.email_address), profile.email.ifBlank { notAdded })
                }
            }
            val business: @Composable () -> Unit = {
                ProfileSection(stringResource(R.string.business_information)) {
                    InfoRow(Icons.Outlined.Storefront, stringResource(R.string.business_name), profile.businessName.ifBlank { notAdded })
                    InfoDivider()
                    InfoRow(Icons.Outlined.Handyman, stringResource(R.string.trade_work_type), profile.trade.ifBlank { notAdded })
                    InfoDivider()
                    InfoRow(Icons.Outlined.LocationOn, stringResource(R.string.business_address), profile.businessAddress.ifBlank { notAdded })
                    InfoDivider()
                    InfoRow(Icons.Outlined.ReceiptLong, stringResource(R.string.gst_number), profile.gstNumber.ifBlank { notAdded })
                }
            }
            val account: @Composable () -> Unit = {
                ProfileSection(stringResource(R.string.account_information)) {
                    InfoRow(
                        Icons.Outlined.CalendarToday,
                        stringResource(R.string.diary_started),
                        startedOn.format(DateTimeFormatter.ofPattern("d MMMM yyyy", Language.locale))
                    )
                    InfoDivider()
                    InfoRow(
                        Icons.Outlined.Key,
                        stringResource(R.string.log_in_with),
                        listOf(profile.mobile, profile.email).filter { it.isNotBlank() }.joinToString("\n").ifBlank { notAdded }
                    )
                    InfoDivider()
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)) {
                        InfoIcon(Icons.Outlined.Language)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(stringResource(R.string.language), style = MaterialTheme.typography.labelMedium, color = InkMuted)
                            Text(stringResource(R.string.language_description), style = MaterialTheme.typography.bodySmall, color = InkMuted)
                        }
                    }
                    // Each language is written in itself, so it can be found whichever one is showing
                    CalmSegmented(
                        options = AppLanguage.entries.map { it.label },
                        selectedIndex = Language.current.ordinal,
                        onSelect = { onLanguageChange(AppLanguage.entries[it]) },
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }

            if (twoColumns) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        personal()
                        contact()
                    }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        business()
                        account()
                    }
                }
            } else {
                personal()
                contact()
                business()
                account()
            }

            OutlinedButton(
                onClick = { confirmLogout = true },
                shape = CircleShape,
                border = BorderStroke(1.dp, ErrorRed),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
            ) {
                Icon(Icons.AutoMirrored.Outlined.Logout, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.log_out), style = MaterialTheme.typography.labelLarge)
            }

            Spacer(Modifier.height(8.dp))
        }
    }

    if (confirmLogout) {
        ConfirmDialog(
            title = stringResource(R.string.log_out_question),
            message = stringResource(R.string.logout_warning_message),
            confirmLabel = stringResource(R.string.log_out),
            onConfirm = {
                confirmLogout = false
                onLogout()
            },
            onDismiss = { confirmLogout = false }
        )
    }
}

/** Photo, name and business at the top, with the way into editing. */
@Composable
private fun IdentityCard(profile: User, startedOn: LocalDate, onEdit: () -> Unit) {
    SoftCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 28.dp, horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ProfileAvatar(name = profile.name, photoPath = profile.photoPath, size = 104.dp)
            Spacer(Modifier.height(16.dp))
            Text(
                profile.name.ifEmpty { stringResource(R.string.user) },
                style = MaterialTheme.typography.headlineMedium,
                color = Ink,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            val business = listOf(profile.businessName, profile.trade).filter { it.isNotBlank() }.joinToString(" · ")
            if (business.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text(business, style = MaterialTheme.typography.bodyLarge, color = SageInk, textAlign = TextAlign.Center)
            }
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.CalendarToday, contentDescription = null, tint = InkMuted, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    stringResource(R.string.member_since_s, startedOn.format(DateTimeFormatter.ofPattern("d MMMM yyyy", Language.locale))),
                    style = MaterialTheme.typography.bodyMedium,
                    color = InkMuted
                )
            }
            Spacer(Modifier.height(20.dp))
            FilledTonalButton(
                onClick = onEdit,
                shape = CircleShape,
                colors = ButtonDefaults.filledTonalButtonColors(containerColor = Mist, contentColor = SageInk),
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.edit_profile), style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun ProfileSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            color = Ink,
            modifier = Modifier
                .padding(start = 4.dp, bottom = 8.dp)
                .semantics { heading() }
        )
        SoftCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp), content = content)
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String) {
    val missing = value == stringResource(R.string.not_added)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .padding(vertical = 10.dp)
    ) {
        InfoIcon(icon)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = InkMuted)
            Text(value, style = MaterialTheme.typography.bodyLarge, color = if (missing) InkMuted else Ink)
        }
    }
}

@Composable
private fun InfoIcon(icon: ImageVector) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(Mist, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = SageInk, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun InfoDivider() {
    HorizontalDivider(color = SandLight, modifier = Modifier.padding(start = 52.dp))
}
