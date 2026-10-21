package dev.nura.app.thozhil.diary.util

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri

/** Opens the phone dialer with the number filled in (no call permission needed). */
fun Context.dial(mobile: String) {
    startActivity(Intent(Intent.ACTION_DIAL, "tel:$mobile".toUri()))
}
