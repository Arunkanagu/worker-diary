package dev.nura.app.thozhil.diary.util

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.content.res.Resources
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import java.util.Locale

/** The languages the screens can be shown in. PDFs stay in English whatever is chosen. */
enum class AppLanguage(val label: String, val code: String) {
    ENGLISH("English", "en"),
    TAMIL("தமிழ்", "ta")
}

/** The language in use. Changing it redraws everything under [ProvideAppLanguage]. */
object Language {
    var current by mutableStateOf(AppLanguage.ENGLISH)

    /** For dates on screen, so month and day names match the strings around them. */
    val locale: Locale get() = Locale.forLanguageTag(current.code)
}

/** The activity with its resources swapped for ones in another locale, so getString and Toasts follow too. */
private class LocalizedContext(base: Context, configuration: Configuration) : ContextWrapper(base) {
    private val localized = base.createConfigurationContext(configuration).resources
    override fun getResources(): Resources = localized
}

/**
 * Shows [content] in [Language.current]. stringResource reads LocalResources, and code reads
 * LocalContext, so both are replaced; overriding LocalConfiguration alone leaves the device language.
 */
@Composable
fun ProvideAppLanguage(content: @Composable () -> Unit) {
    val base = LocalContext.current
    val code = Language.current.code
    val configuration = Configuration(LocalConfiguration.current).apply {
        setLocale(Locale.forLanguageTag(code))
    }
    val context = remember(base, configuration) { LocalizedContext(base, configuration) }
    CompositionLocalProvider(
        LocalConfiguration provides configuration,
        LocalContext provides context,
        LocalResources provides context.resources,
        content = content
    )
}
