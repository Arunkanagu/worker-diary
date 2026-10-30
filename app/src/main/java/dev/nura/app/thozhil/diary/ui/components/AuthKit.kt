package dev.nura.app.thozhil.diary.ui.components

import android.util.Patterns
import androidx.annotation.StringRes
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import dev.nura.app.thozhil.diary.R
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.selection.selectable
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import dev.nura.app.thozhil.diary.ui.theme.CoverHigh
import dev.nura.app.thozhil.diary.util.AppLanguage
import dev.nura.app.thozhil.diary.ui.theme.Amber
import dev.nura.app.thozhil.diary.ui.theme.Cover
import dev.nura.app.thozhil.diary.ui.theme.InkMuted
import dev.nura.app.thozhil.diary.ui.theme.OnCoverMuted
import dev.nura.app.thozhil.diary.ui.theme.Sage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** What a password needs. Each rule shows as a line that ticks off while typing. */
enum class PasswordRule(@StringRes val label: Int, val passes: (String) -> Boolean) {
    LENGTH(R.string.password_rule_length, { it.length >= 8 }),
    UPPER(R.string.password_rule_upper, { pw -> pw.any { it.isUpperCase() } }),
    LOWER(R.string.password_rule_lower, { pw -> pw.any { it.isLowerCase() } }),
    DIGIT(R.string.password_rule_digit, { pw -> pw.any { it.isDigit() } }),
    SPECIAL(R.string.password_rule_special, { pw -> pw.any { !it.isLetterOrDigit() && !it.isWhitespace() } });

    companion object {
        fun allPass(password: String) = entries.all { it.passes(password) }
    }
}

/** A 10 digit Indian mobile number, with or without +91 in front. */
fun isValidMobile(mobile: String): Boolean {
    val digits = mobile.trim().removePrefix("+91").filter { it != ' ' && it != '-' }
    return digits.length == 10 && digits.all { it.isDigit() } && digits.first() in '6'..'9'
}

fun isValidEmail(email: String): Boolean = Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()

/**
 * The sign-in pages: the diary cover from the splash with a title, and a sheet on its bottom edge.
 * On a tablet the content keeps a comfortable reading width in the middle.
 */
@Composable
fun AuthScaffold(
    title: String,
    description: String,
    onBack: (() -> Unit)? = null,
    /** Beside the logo at the top of the cover, e.g. the language switch. */
    coverAction: @Composable () -> Unit = {},
    footer: @Composable ColumnScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = MaterialTheme.colorScheme
    CoverStatusBar()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Cover, RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                .statusBarsPadding(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 560.dp)
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = if (onBack != null) 8.dp else 40.dp, bottom = 72.dp)
            ) {
                if (onBack != null) {
                    IconButton(onClick = onBack, modifier = Modifier.offset(x = (-12).dp)) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = Color.White
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                } else {
                    Row(verticalAlignment = Alignment.Top) {
                        Box {
                            // The amber ribbon hangs from under the book, as on the splash
                            Box(
                                modifier = Modifier
                                    .padding(start = 44.dp, top = 40.dp)
                                    .size(width = 14.dp, height = 46.dp)
                                    .background(Amber, AuthRibbonShape)
                            )
                            AppLogo(size = 64.dp)
                        }
                        Spacer(Modifier.weight(1f))
                        coverAction()
                    }
                    Spacer(Modifier.height(36.dp))
                }
                // Shrinks to fit rather than breaking a long word ("வரவேற்கிறோம்") across lines
                BasicText(
                    text = title,
                    style = MaterialTheme.typography.displayMedium.copy(color = Color.White),
                    maxLines = 1,
                    autoSize = TextAutoSize.StepBased(minFontSize = 24.sp, maxFontSize = MaterialTheme.typography.displayMedium.fontSize)
                )
                Spacer(Modifier.height(8.dp))
                Text(text = description, style = MaterialTheme.typography.bodyLarge, color = OnCoverMuted)
            }
        }

        SoftCard(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .offset(y = (-40).dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), content = content)
        }

        Column(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 24.dp)
                .offset(y = (-24).dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = footer
        )
    }
}

/** A labelled, rounded field on the sign-in sheet. */
@Composable
fun AuthTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector,
    modifier: Modifier = Modifier,
    error: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.None,
    imeAction: ImeAction = ImeAction.Next,
    onDone: () -> Unit = {},
    visualTransformation: VisualTransformation = VisualTransformation.None,
    singleLine: Boolean = true,
    focusRequester: FocusRequester? = null,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    val colors = MaterialTheme.colorScheme
    val bringIntoView = remember { BringIntoViewRequester() }
    val scope = rememberCoroutineScope()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .bringIntoViewRequester(bringIntoView)
            // The text field only scrolls its cursor clear of the keyboard; once the keyboard
            // has come up, show the whole field with its label and error too
            .onFocusEvent { state ->
                if (state.hasFocus) scope.launch {
                    delay(350)
                    bringIntoView.bringIntoView()
                }
            }
    ) {
        Text(label, style = MaterialTheme.typography.titleSmall, color = colors.onSurface)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder) },
            leadingIcon = { Icon(leadingIcon, contentDescription = null) },
            trailingIcon = trailingIcon,
            isError = error != null,
            supportingText = error?.let { { Text(it) } },
            singleLine = singleLine,
            minLines = if (singleLine) 1 else 2,
            visualTransformation = visualTransformation,
            keyboardOptions = KeyboardOptions(
                capitalization = capitalization,
                keyboardType = keyboardType,
                imeAction = imeAction
            ),
            keyboardActions = KeyboardActions(onDone = { onDone() }),
            // A pill for one line; a field that grows gets softer corners instead
            shape = if (singleLine) CircleShape else RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White,
                errorContainerColor = Color.White,
                focusedBorderColor = colors.primary,
                unfocusedLeadingIconColor = colors.onSurfaceVariant,
                focusedLeadingIconColor = colors.primary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
        )
    }
}

/** A password field with an eye to show or hide what was typed. */
@Composable
fun PasswordField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    imeAction: ImeAction = ImeAction.Next,
    onDone: () -> Unit = {}
) {
    var visible by rememberSaveable { mutableStateOf(false) }
    AuthTextField(
        label = label,
        value = value,
        onValueChange = onValueChange,
        placeholder = placeholder,
        leadingIcon = Icons.Outlined.Lock,
        modifier = modifier,
        error = error,
        keyboardType = KeyboardType.Password,
        imeAction = imeAction,
        onDone = onDone,
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = { visible = !visible }) {
                Icon(
                    if (visible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                    contentDescription = stringResource(if (visible) R.string.hide_password else R.string.show_password)
                )
            }
        }
    )
}

/** The password rules, each ticked green once the typed password meets it. */
@Composable
fun PasswordRules(password: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        PasswordRule.entries.forEach { rule ->
            val met = rule.passes(password)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (met) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
                    contentDescription = null,
                    tint = if (met) Sage else InkMuted,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    stringResource(rule.label),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (met) Sage else InkMuted
                )
            }
        }
    }
}

/** The main action on a sign-in sheet. */
@Composable
fun AuthButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        shape = CircleShape,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.width(10.dp))
        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(20.dp))
    }
}

/**
 * English / தமிழ் on the dark cover, for choosing the language before signing in. Each is written
 * in itself, so it can be found whichever one is showing.
 */
@Composable
fun CoverLanguageSwitch(selected: AppLanguage, onSelect: (AppLanguage) -> Unit) {
    val description = stringResource(R.string.language)
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(CoverHigh)
            .padding(4.dp)
            .semantics { contentDescription = description }
    ) {
        AppLanguage.entries.forEach { language ->
            val isSelected = language == selected
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (isSelected) Color.White else Color.Transparent)
                    .selectable(selected = isSelected, role = Role.RadioButton) { onSelect(language) }
                    .heightIn(min = 40.dp)
                    .padding(horizontal = 14.dp)
            ) {
                Text(
                    language.label,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isSelected) Cover else OnCoverMuted,
                    maxLines = 1
                )
            }
        }
    }
}

/** "Don't have an account? Sign up" under the sheet. */
@Composable
fun AuthSwitchLine(prompt: String, action: String, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(prompt, style = MaterialTheme.typography.bodyMedium, color = InkMuted)
        TextButton(onClick = onClick) {
            Text(action, style = MaterialTheme.typography.labelLarge)
        }
    }
}

/** A bookmark ribbon with a V-notch at the bottom. */
private val AuthRibbonShape = GenericShape { size, _ ->
    moveTo(0f, 0f)
    lineTo(size.width, 0f)
    lineTo(size.width, size.height)
    lineTo(size.width / 2f, size.height - size.width * 0.55f)
    lineTo(0f, size.height)
    close()
}
