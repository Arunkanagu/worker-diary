package dev.nura.app.thozhil.diary.util

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedPrefHelper @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("thozhil_diary_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_USER_NAME = "key_user_name"
        private const val KEY_STARTED_ON = "key_started_on"
    }

    fun getUserName(): String {
        return prefs.getString(KEY_USER_NAME, "") ?: ""
    }

    /** Saves the name; the first save also records the day the diary was started. */
    fun saveUserName(name: String) {
        val editor = prefs.edit().putString(KEY_USER_NAME, name.trim())
        if (!prefs.contains(KEY_STARTED_ON)) {
            editor.putString(KEY_STARTED_ON, LocalDate.now().toString())
        }
        editor.apply()
    }

    /** The day the diary was started, or today if it was never recorded. */
    fun getStartedOn(): LocalDate {
        return prefs.getString(KEY_STARTED_ON, null)
            ?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
            ?: LocalDate.now()
    }

    fun clearUserName() {
        prefs.edit().remove(KEY_USER_NAME).remove(KEY_STARTED_ON).apply()
    }

    fun isLoggedIn(): Boolean {
        return getUserName().isNotBlank()
    }
}
