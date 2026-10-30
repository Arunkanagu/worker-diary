package dev.nura.app.thozhil.diary.ui.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Handyman
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dev.nura.app.thozhil.diary.R
import dev.nura.app.thozhil.diary.ui.components.AuthTextField
import dev.nura.app.thozhil.diary.ui.components.ProfileAvatar
import dev.nura.app.thozhil.diary.ui.components.ScreenHeader
import dev.nura.app.thozhil.diary.ui.components.SoftCard
import dev.nura.app.thozhil.diary.ui.components.isValidEmail
import dev.nura.app.thozhil.diary.ui.components.isValidMobile
import dev.nura.app.thozhil.diary.ui.theme.ErrorRed
import dev.nura.app.thozhil.diary.ui.theme.Ink
import dev.nura.app.thozhil.diary.ui.theme.IvoryCard
import dev.nura.app.thozhil.diary.ui.theme.SageDeep
import dev.nura.app.thozhil.diary.ui.theme.SandLight
import dev.nura.app.thozhil.diary.data.User
import dev.nura.app.thozhil.diary.util.deleteProfilePhoto
import dev.nura.app.thozhil.diary.util.saveProfilePhoto
import kotlinx.coroutines.launch

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

/** A GSTIN: state code, PAN, entity number, Z, check character. */
private val GstPattern = Regex("^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z][1-9A-Z]Z[0-9A-Z]$")

@Composable
fun EditProfileScreen(
    profile: User,
    onSave: (User) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var name by rememberSaveable { mutableStateOf(profile.name) }
    var mobile by rememberSaveable { mutableStateOf(profile.mobile) }
    var email by rememberSaveable { mutableStateOf(profile.email) }
    var businessName by rememberSaveable { mutableStateOf(profile.businessName) }
    var trade by rememberSaveable { mutableStateOf(profile.trade) }
    var address by rememberSaveable { mutableStateOf(profile.businessAddress) }
    var gst by rememberSaveable { mutableStateOf(profile.gstNumber) }
    // The photo shown here; a newly picked one is copied in straight away so it can be previewed
    var photoPath by rememberSaveable { mutableStateOf(profile.photoPath) }
    var photoBusy by remember { mutableStateOf(false) }
    var saved by remember { mutableStateOf(false) }

    var nameError by rememberSaveable { mutableStateOf<String?>(null) }
    var mobileError by rememberSaveable { mutableStateOf<String?>(null) }
    var emailError by rememberSaveable { mutableStateOf<String?>(null) }
    var gstError by rememberSaveable { mutableStateOf<String?>(null) }
    var photoError by rememberSaveable { mutableStateOf<String?>(null) }

    val nameRequired = stringResource(R.string.please_enter_your_name)
    val mobileRequired = stringResource(R.string.please_enter_your_mobile)
    val mobileInvalid = stringResource(R.string.enter_valid_mobile)
    val emailInvalid = stringResource(R.string.enter_valid_email)
    val gstInvalid = stringResource(R.string.enter_valid_gst)
    val photoFailed = stringResource(R.string.photo_could_not_be_used)
    val nameFocus = remember { FocusRequester() }
    val mobileFocus = remember { FocusRequester() }
    val emailFocus = remember { FocusRequester() }
    val gstFocus = remember { FocusRequester() }

    // A photo picked but not saved is thrown away when leaving, however that happens; a rotation
    // isn't leaving, and the form keeps pointing at the photo
    DisposableEffect(Unit) {
        onDispose {
            val rotating = context.findActivity()?.isChangingConfigurations == true
            if (!saved && !rotating && photoPath != profile.photoPath) deleteProfilePhoto(photoPath)
        }
    }

    val pickPhoto = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        photoBusy = true
        photoError = null
        scope.launch {
            // Replaces an earlier unsaved pick, never the saved photo
            val previous = if (photoPath != profile.photoPath) photoPath else ""
            val path = saveProfilePhoto(context, uri, previous)
            if (path != null) photoPath = path else photoError = photoFailed
            photoBusy = false
        }
    }

    fun save() {
        nameError = if (name.isBlank()) nameRequired else null
        mobileError = when {
            mobile.isBlank() -> mobileRequired
            !isValidMobile(mobile) -> mobileInvalid
            else -> null
        }
        emailError = if (email.isNotBlank() && !isValidEmail(email)) emailInvalid else null
        gstError = if (gst.isNotBlank() && !GstPattern.matches(gst.trim().uppercase())) gstInvalid else null
        // Go to the first field to fix, which scrolls it and its message into view
        val firstError = listOf(nameError to nameFocus, mobileError to mobileFocus, emailError to emailFocus, gstError to gstFocus)
            .firstOrNull { it.first != null }
        if (firstError != null) {
            firstError.second.requestFocus()
            return
        }
        // The old photo goes once the new one (or none) is saved in its place
        if (photoPath != profile.photoPath) deleteProfilePhoto(profile.photoPath)
        saved = true
        onSave(
            profile.copy(
                name = name.trim(),
                mobile = mobile.trim(),
                email = email.trim(),
                businessName = businessName.trim(),
                trade = trade.trim(),
                businessAddress = address.trim(),
                gstNumber = gst.trim().uppercase(),
                photoPath = photoPath
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        ScreenHeader(
            title = stringResource(R.string.edit_profile),
            onBack = onCancel,
            trail = listOf(stringResource(R.string.profile)),
            modifier = Modifier.padding(start = 8.dp, end = 20.dp)
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 640.dp)
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Photo
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .clickable(role = Role.Button, onClickLabel = stringResource(R.string.change_photo)) {
                                pickPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            }
                    ) {
                        ProfileAvatar(name = name, photoPath = photoPath, size = 112.dp, showDot = false)
                        if (photoBusy) CircularProgressIndicator(color = SageDeep)
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(36.dp)
                                .background(SageDeep, CircleShape)
                                .border(3.dp, IvoryCard, CircleShape)
                        ) {
                            Icon(Icons.Outlined.PhotoCamera, contentDescription = null, tint = IvoryCard, modifier = Modifier.size(18.dp))
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = { pickPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) {
                            Text(stringResource(if (photoPath.isBlank()) R.string.add_photo else R.string.change_photo), color = SageDeep)
                        }
                        if (photoPath.isNotBlank()) {
                            TextButton(onClick = {
                                if (photoPath != profile.photoPath) deleteProfilePhoto(photoPath)
                                photoPath = ""
                            }) {
                                Text(stringResource(R.string.remove_photo), color = ErrorRed)
                            }
                        }
                    }
                    photoError?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = ErrorRed) }
                }

                EditSection(stringResource(R.string.personal_information)) {
                    AuthTextField(
                        label = stringResource(R.string.full_name_required),
                        value = name,
                        onValueChange = { name = it; nameError = null },
                        placeholder = stringResource(R.string.enter_your_name),
                        leadingIcon = Icons.Outlined.Person,
                        error = nameError,
                        focusRequester = nameFocus,
                        capitalization = KeyboardCapitalization.Words
                    )
                }

                EditSection(stringResource(R.string.contact_information)) {
                    AuthTextField(
                        label = stringResource(R.string.mobile_number_required),
                        value = mobile,
                        onValueChange = { mobile = it.filter { c -> c.isDigit() || c == '+' || c == ' ' }; mobileError = null },
                        placeholder = stringResource(R.string.mobile_placeholder),
                        leadingIcon = Icons.Outlined.Phone,
                        error = mobileError,
                        focusRequester = mobileFocus,
                        keyboardType = KeyboardType.Phone
                    )
                    AuthTextField(
                        label = stringResource(R.string.email_optional),
                        value = email,
                        onValueChange = { email = it; emailError = null },
                        placeholder = stringResource(R.string.email_placeholder),
                        leadingIcon = Icons.Outlined.Email,
                        error = emailError,
                        focusRequester = emailFocus,
                        keyboardType = KeyboardType.Email
                    )
                    Text(
                        stringResource(R.string.login_details_change_note),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                EditSection(stringResource(R.string.business_information)) {
                    AuthTextField(
                        label = stringResource(R.string.business_name),
                        value = businessName,
                        onValueChange = { businessName = it },
                        placeholder = stringResource(R.string.business_name_placeholder),
                        leadingIcon = Icons.Outlined.Storefront,
                        capitalization = KeyboardCapitalization.Words
                    )
                    AuthTextField(
                        label = stringResource(R.string.trade_work_type),
                        value = trade,
                        onValueChange = { trade = it },
                        placeholder = stringResource(R.string.trade_placeholder),
                        leadingIcon = Icons.Outlined.Handyman,
                        capitalization = KeyboardCapitalization.Words
                    )
                    AuthTextField(
                        label = stringResource(R.string.business_address),
                        value = address,
                        onValueChange = { address = it },
                        placeholder = stringResource(R.string.business_address_placeholder),
                        leadingIcon = Icons.Outlined.LocationOn,
                        capitalization = KeyboardCapitalization.Words,
                        singleLine = false
                    )
                    AuthTextField(
                        label = stringResource(R.string.gst_number_optional),
                        value = gst,
                        onValueChange = { gst = it.uppercase().filter { c -> c.isLetterOrDigit() }.take(15); gstError = null },
                        placeholder = stringResource(R.string.gst_placeholder),
                        leadingIcon = Icons.Outlined.ReceiptLong,
                        error = gstError,
                        focusRequester = gstFocus,
                        capitalization = KeyboardCapitalization.Characters,
                        imeAction = ImeAction.Done
                    )
                }
            }
        }

        // Save and Cancel stay in reach however far the form is scrolled
        HorizontalDivider(color = SandLight)
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                shape = CircleShape,
                border = BorderStroke(1.dp, SandLight),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Ink),
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 52.dp)
            ) { Text(stringResource(R.string.cancel), style = MaterialTheme.typography.labelLarge) }
            Button(
                onClick = ::save,
                enabled = !photoBusy,
                shape = CircleShape,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 52.dp)
            ) { Text(stringResource(R.string.save), style = MaterialTheme.typography.labelLarge) }
        }
    }
}

@Composable
private fun EditSection(title: String, content: @Composable ColumnScope.() -> Unit) {
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
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                content = content
            )
        }
    }
}
