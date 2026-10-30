package dev.nura.app.thozhil.diary

import dev.nura.app.thozhil.diary.data.AccountRepository
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import dev.nura.app.thozhil.diary.ui.navigation.AppNavigation
import dev.nura.app.thozhil.diary.ui.theme.ThozhildiaryTheme
import dev.nura.app.thozhil.diary.util.Language
import dev.nura.app.thozhil.diary.util.SharedPrefHelper
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var sharedPrefHelper: SharedPrefHelper

    @Inject
    lateinit var accounts: AccountRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Language.current = sharedPrefHelper.getLanguage()
        // Phones stay in portrait; tablets (smallest width 600dp and up) turn freely.
        // Set both ways: the request outlives recreation, e.g. when a foldable opens out.
        requestedOrientation = if (resources.configuration.smallestScreenWidthDp < 600) {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        } else {
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
        // The app is always on light ivory, so system bar icons stay dark.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )
        setContent {
            ThozhildiaryTheme {
                AppNavigation(sharedPrefHelper = sharedPrefHelper, accounts = accounts)
            }
        }
    }
}
