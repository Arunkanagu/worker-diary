package dev.nura.app.thozhil.diary.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import dev.nura.app.thozhil.diary.ui.components.AppLogo
import dev.nura.app.thozhil.diary.ui.components.CalmProgressBar
import dev.nura.app.thozhil.diary.util.SharedPrefHelper
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    sharedPrefHelper: SharedPrefHelper,
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    val appear = remember { Animatable(0f) }
    val loading = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch { appear.animateTo(1f, tween(450, easing = FastOutSlowInEasing)) }
        loading.animateTo(1f, tween(1400, easing = LinearEasing))
        if (sharedPrefHelper.isLoggedIn()) {
            onNavigateToHome()
        } else {
            onNavigateToLogin()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.graphicsLayer {
                alpha = appear.value
                translationY = (1f - appear.value) * 12.dp.toPx()
            }
        ) {
            AppLogo(size = 104.dp)
            Spacer(Modifier.height(28.dp))
            Text(
                text = "Thozhil Diary",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Your daily work diary",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(36.dp))
            CalmProgressBar(progress = loading.value, height = 6.dp, modifier = Modifier.width(140.dp))
        }
    }
}
