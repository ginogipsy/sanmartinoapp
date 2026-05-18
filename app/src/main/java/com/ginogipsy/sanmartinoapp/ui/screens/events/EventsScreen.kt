package com.ginogipsy.sanmartinoapp.ui.screens.events

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ginogipsy.sanmartinoapp.R
import com.ginogipsy.sanmartinoapp.data.model.Event
import com.ginogipsy.sanmartinoapp.ui.components.BilingualDescriptionDialog
import com.ginogipsy.sanmartinoapp.ui.components.LanguageToggle
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsScreen(
    onOpenCantine: () -> Unit,
    viewModel: EventsViewModel = viewModel(),
) {
    val state = viewModel.uiState
    var dialogEvent by remember { mutableStateOf<Event?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.events_title)) },
                actions = { LanguageToggle() },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp),
        ) {
            if (state.upcoming.isNotEmpty()) {
                item { SectionHeader(stringResource(R.string.events_section_upcoming)) }
                items(state.upcoming, key = { it.id }) { event ->
                    EventCard(
                        event = event,
                        showCantineButton = true,
                        onDescription = { dialogEvent = event },
                        onOpenCantine = onOpenCantine,
                    )
                }
            }
            if (state.past.isNotEmpty()) {
                item { SectionHeader(stringResource(R.string.events_section_past)) }
                items(state.past, key = { it.id }) { event ->
                    EventCard(
                        event = event,
                        showCantineButton = false,
                        onDescription = { dialogEvent = event },
                        onOpenCantine = onOpenCantine,
                    )
                }
            }
        }
    }

    dialogEvent?.let { event ->
        BilingualDescriptionDialog(
            title = event.name,
            description = event.description,
            onDismiss = { dialogEvent = null },
        )
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
    )
}

@Composable
private fun EventCard(
    event: Event,
    showCantineButton: Boolean,
    onDescription: () -> Unit,
    onOpenCantine: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = event.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = event.place,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = formatDateRange(event),
                style = MaterialTheme.typography.bodyMedium,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(onClick = onDescription) {
                    Text(stringResource(R.string.action_description))
                }
                if (showCantineButton) {
                    Button(onClick = onOpenCantine) {
                        Icon(
                            imageVector = Icons.Filled.Storefront,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 6.dp),
                        )
                        Text(stringResource(R.string.action_open_cantine))
                    }
                }
            }
        }
    }
}

private val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ITALY)

private fun formatDateRange(event: Event): String {
    val start = event.startDate.format(dateFormatter)
    val end = event.endDate.format(dateFormatter)
    return if (start == end) start else "$start — $end"
}
