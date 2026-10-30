package dev.nura.app.thozhil.diary.ui.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dev.nura.app.thozhil.diary.R
import dev.nura.app.thozhil.diary.ui.components.AuthButton
import dev.nura.app.thozhil.diary.ui.components.AuthScaffold
import dev.nura.app.thozhil.diary.ui.components.AuthSwitchLine
import dev.nura.app.thozhil.diary.ui.components.AuthTextField
import dev.nura.app.thozhil.diary.ui.components.PasswordField
import dev.nura.app.thozhil.diary.ui.components.PasswordRule
import dev.nura.app.thozhil.diary.ui.components.PasswordRules
import dev.nura.app.thozhil.diary.ui.components.isValidEmail
import dev.nura.app.thozhil.diary.ui.components.isValidMobile

@Composable
fun SignUpScreen(
    /** Whether an account is already set up on this phone; the diary keeps one. */
    accountExists: Boolean,
    onSignUp: (name: String, mobile: String, email: String, password: String) -> Unit,
    onBack: () -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var mobile by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirm by rememberSaveable { mutableStateOf("") }

    var nameError by rememberSaveable { mutableStateOf<String?>(null) }
    var mobileError by rememberSaveable { mutableStateOf<String?>(null) }
    var emailError by rememberSaveable { mutableStateOf<String?>(null) }
    var passwordError by rememberSaveable { mutableStateOf<String?>(null) }
    var confirmError by rememberSaveable { mutableStateOf<String?>(null) }
    var formError by rememberSaveable { mutableStateOf<String?>(null) }

    val nameRequired = stringResource(R.string.please_enter_your_name)
    val mobileRequired = stringResource(R.string.please_enter_your_mobile)
    val mobileInvalid = stringResource(R.string.enter_valid_mobile)
    val emailInvalid = stringResource(R.string.enter_valid_email)
    val passwordWeak = stringResource(R.string.password_does_not_meet_rules)
    val passwordMismatch = stringResource(R.string.passwords_do_not_match)
    val alreadySetUp = stringResource(R.string.account_already_exists)

    fun submit() {
        nameError = if (name.isBlank()) nameRequired else null
        mobileError = when {
            mobile.isBlank() -> mobileRequired
            !isValidMobile(mobile) -> mobileInvalid
            else -> null
        }
        // Email is optional, but if given it has to be one we can match at login
        emailError = if (email.isNotBlank() && !isValidEmail(email)) emailInvalid else null
        passwordError = if (!PasswordRule.allPass(password)) passwordWeak else null
        confirmError = if (confirm != password) passwordMismatch else null
        formError = if (accountExists) alreadySetUp else null
        if (listOf(nameError, mobileError, emailError, passwordError, confirmError, formError).any { it != null }) return
        onSignUp(name.trim(), mobile.trim(), email.trim(), password)
    }

    AuthScaffold(
        title = stringResource(R.string.create_account),
        description = stringResource(R.string.sign_up_description),
        onBack = onBack,
        footer = {
            AuthSwitchLine(
                prompt = stringResource(R.string.already_have_account),
                action = stringResource(R.string.log_in),
                onClick = onBack
            )
        }
    ) {
        AuthTextField(
            label = stringResource(R.string.user_name),
            value = name,
            onValueChange = { name = it; nameError = null },
            placeholder = stringResource(R.string.enter_your_name),
            leadingIcon = Icons.Outlined.Person,
            error = nameError,
            capitalization = KeyboardCapitalization.Words
        )
        Spacer(Modifier.height(12.dp))
        AuthTextField(
            label = stringResource(R.string.mobile_number_required),
            value = mobile,
            onValueChange = { mobile = it.filter { c -> c.isDigit() || c == '+' || c == ' ' }; mobileError = null },
            placeholder = stringResource(R.string.mobile_placeholder),
            leadingIcon = Icons.Outlined.Phone,
            error = mobileError,
            keyboardType = KeyboardType.Phone
        )
        Spacer(Modifier.height(12.dp))
        AuthTextField(
            label = stringResource(R.string.email_optional),
            value = email,
            onValueChange = { email = it; emailError = null },
            placeholder = stringResource(R.string.email_placeholder),
            leadingIcon = Icons.Outlined.Email,
            error = emailError,
            keyboardType = KeyboardType.Email
        )
        Spacer(Modifier.height(12.dp))
        PasswordField(
            label = stringResource(R.string.password),
            value = password,
            onValueChange = { password = it; passwordError = null },
            placeholder = stringResource(R.string.create_a_password),
            error = passwordError
        )
        PasswordRules(password = password, modifier = Modifier.padding(start = 8.dp, top = 4.dp))
        Spacer(Modifier.height(16.dp))
        PasswordField(
            label = stringResource(R.string.confirm_password),
            value = confirm,
            onValueChange = { confirm = it; confirmError = null },
            placeholder = stringResource(R.string.type_password_again),
            error = confirmError,
            imeAction = ImeAction.Done,
            onDone = ::submit
        )
        formError?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(20.dp))
        AuthButton(text = stringResource(R.string.sign_up), onClick = ::submit)
    }
}
