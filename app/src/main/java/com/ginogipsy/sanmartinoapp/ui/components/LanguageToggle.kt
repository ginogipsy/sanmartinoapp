package com.ginogipsy.sanmartinoapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ginogipsy.sanmartinoapp.data.model.Language
import com.ginogipsy.sanmartinoapp.i18n.LocalLanguage

@Composable
fun LanguageToggle(modifier: Modifier = Modifier) {
    var lang by LocalLanguage.current
    Row(
        modifier = modifier.padding(end = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        FilterChip(
            selected = lang == Language.IT,
            onClick = { lang = Language.IT },
            label = { Text("IT") },
        )
        FilterChip(
            selected = lang == Language.EN,
            onClick = { lang = Language.EN },
            label = { Text("EN") },
        )
    }
}
