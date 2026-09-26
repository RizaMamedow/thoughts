package dev.thoughts.app.ui.features.thoughts.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.thoughts.app.R
import dev.thoughts.app.data.settings.ThemeMode

@Composable
fun AppDrawerContent(
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    onAboutClick: () -> Unit,
) {
    ModalDrawerSheet {
        Text(
            stringResource(R.string.app_name),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(16.dp),
        )

        HorizontalDivider()

        Text(
            stringResource(R.string.settings_theme),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
        ThemeModeSelector(selected = themeMode, onSelect = onThemeModeChange)

        Text(
            stringResource(R.string.settings_language),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
        LanguageSelector()

        HorizontalDivider(modifier = Modifier.padding(top = 8.dp))

        NavigationDrawerItem(
            label = { Text(stringResource(R.string.drawer_about)) },
            icon = { Icon(Icons.Default.Info, contentDescription = null) },
            selected = false,
            onClick = onAboutClick,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
        )
    }
}
