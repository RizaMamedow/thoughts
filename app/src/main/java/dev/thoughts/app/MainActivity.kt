package dev.thoughts.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.navigator.Navigator
import dev.thoughts.app.data.settings.SettingsRepository
import dev.thoughts.app.data.settings.ThemeMode
import dev.thoughts.app.ui.features.thoughts.ThoughtsScreen
import dev.thoughts.app.ui.theme.ThoughtsTheme
import org.koin.android.ext.android.inject

class MainActivity : AppCompatActivity() {
    private val settingsRepository: SettingsRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by settingsRepository.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
            ThoughtsTheme(themeMode = themeMode) {
                Navigator(ThoughtsScreen())
            }
        }
    }
}
