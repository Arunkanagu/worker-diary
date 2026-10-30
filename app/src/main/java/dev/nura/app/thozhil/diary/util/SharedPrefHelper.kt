package dev.nura.app.thozhil.diary.util

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.nura.app.thozhil.diary.data.AppDatabase
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Settings for this phone: the screen language and PDF style. Accounts live in the database's users
 * table; this only still reads the account as it was kept here before, to move it across.
 */
@Singleton
class SharedPrefHelper @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("thozhil_diary_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_PDF_STYLE = "key_pdf_style"
        private const val KEY_LANGUAGE = "key_language"

        // Where the account was kept before it moved into the users table
        private const val KEY_USER_NAME = "key_user_name"
        private const val KEY_STARTED_ON = "key_started_on"
        private const val KEY_MOBILE = "key_mobile"
        private const val KEY_EMAIL = "key_email"
        private const val KEY_PASSWORD_SALT = "key_password_salt"
        private const val KEY_PASSWORD_HASH = "key_password_hash"
        private const val KEY_LOGGED_IN = "key_logged_in"
        private const val KEY_BUSINESS_NAME = "key_business_name"
        private const val KEY_TRADE = "key_trade"
        private const val KEY_BUSINESS_ADDRESS = "key_business_address"
        private const val KEY_GST_NUMBER = "key_gst_number"
        private const val KEY_PHOTO_PATH = "key_photo_path"

        private val LegacyAccountKeys = listOf(
            KEY_USER_NAME, KEY_STARTED_ON, KEY_MOBILE, KEY_EMAIL, KEY_PASSWORD_SALT, KEY_PASSWORD_HASH,
            KEY_LOGGED_IN, KEY_BUSINESS_NAME, KEY_TRADE, KEY_BUSINESS_ADDRESS, KEY_GST_NUMBER, KEY_PHOTO_PATH
        )
    }

    /** The look picked last time a PDF was previewed. */
    fun getPdfStyle(): PdfStyle =
        prefs.getString(KEY_PDF_STYLE, null)
            ?.let { runCatching { PdfStyle.valueOf(it) }.getOrNull() }
            ?: PdfStyle.CLASSIC

    fun savePdfStyle(style: PdfStyle) {
        prefs.edit().putString(KEY_PDF_STYLE, style.name).apply()
    }

    /** The language the app's screens are shown in; PDFs are always English. */
    fun getLanguage(): AppLanguage =
        prefs.getString(KEY_LANGUAGE, null)
            ?.let { runCatching { AppLanguage.valueOf(it) }.getOrNull() }
            ?: AppLanguage.ENGLISH

    fun saveLanguage(language: AppLanguage) {
        prefs.edit().putString(KEY_LANGUAGE, language.name).apply()
    }

    /**
     * The account as kept here before version 12 of the database, or null if there was none. A name
     * saved before there were passwords comes back as a logged-in user with no password.
     */
    fun readLegacyAccount(): AppDatabase.Companion.LegacyAccount? {
        val name = prefs.getString(KEY_USER_NAME, "").orEmpty()
        val hash = prefs.getString(KEY_PASSWORD_HASH, "").orEmpty()
        if (name.isBlank() && hash.isBlank()) return null
        val startedOn = prefs.getString(KEY_STARTED_ON, null)?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
        return AppDatabase.Companion.LegacyAccount(
            name = name,
            mobile = prefs.getString(KEY_MOBILE, "").orEmpty(),
            email = prefs.getString(KEY_EMAIL, "").orEmpty(),
            passwordSalt = prefs.getString(KEY_PASSWORD_SALT, "").orEmpty(),
            passwordHash = hash,
            businessName = prefs.getString(KEY_BUSINESS_NAME, "").orEmpty(),
            trade = prefs.getString(KEY_TRADE, "").orEmpty(),
            businessAddress = prefs.getString(KEY_BUSINESS_ADDRESS, "").orEmpty(),
            gstNumber = prefs.getString(KEY_GST_NUMBER, "").orEmpty(),
            photoPath = prefs.getString(KEY_PHOTO_PATH, "").orEmpty(),
            createdAt = startedOn?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli() ?: System.currentTimeMillis(),
            loggedIn = if (hash.isBlank()) true else prefs.getBoolean(KEY_LOGGED_IN, false)
        )
    }

    /** Forgets the old copy once the account is in the database. */
    fun clearLegacyAccount() {
        if (LegacyAccountKeys.none(prefs::contains)) return
        prefs.edit().apply { LegacyAccountKeys.forEach(::remove) }.apply()
    }
}
