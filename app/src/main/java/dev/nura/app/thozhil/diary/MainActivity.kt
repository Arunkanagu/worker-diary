package dev.nura.app.thozhil.diary

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import dev.nura.app.thozhil.diary.ui.navigation.AppNavigation
import dev.nura.app.thozhil.diary.ui.theme.ThozhildiaryTheme
import dev.nura.app.thozhil.diary.util.SharedPrefHelper
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var sharedPrefHelper: SharedPrefHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // The app is always on light ivory, so system bar icons stay dark.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )
        setContent {
            ThozhildiaryTheme {
                AppNavigation(sharedPrefHelper = sharedPrefHelper)
            }
        }
    }
}
