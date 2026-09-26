package dev.thoughts.app.ui.features.thoughts.components

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import dev.thoughts.app.R
import java.util.Locale

private val supportedLanguages = listOf("ru", "en")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSelector() {
    val appLocales = AppCompatDelegate.getApplicationLocales()
    val current = if (!appLocales.isEmpty) appLocales[0]?.language else Locale.getDefault().language
    val options = listOf(
        "ru" to stringResource(R.string.language_russian),
        "en" to stringResource(R.string.language_english),
    )
    SingleChoiceSegmentedButtonRow(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
    ) {
        options.forEachIndexed { index, (tag, label) ->
            SegmentedButton(
                selected = current == tag || (current !in supportedLanguages && tag == "en"),
                onClick = { AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag)) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
            ) {
                Text(label)
            }
        }
    }
}
