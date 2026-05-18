package com.ginogipsy.sanmartinoapp.ui.screens.menu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ginogipsy.sanmartinoapp.R
import com.ginogipsy.sanmartinoapp.data.model.MenuItem
import com.ginogipsy.sanmartinoapp.data.model.MenuKind
import com.ginogipsy.sanmartinoapp.ui.components.BilingualDescriptionDialog
import com.ginogipsy.sanmartinoapp.ui.components.LanguageToggle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    cantinaId: String,
    kind: MenuKind,
    onBack: () -> Unit,
) {
    val viewModel: MenuViewModel = viewModel(
        key = "menu-$cantinaId-${kind.name}",
        factory = MenuViewModel.factory(cantinaId, kind),
    )
    var dialogItem by remember { mutableStateOf<MenuItem?>(null) }

    val titleRes = if (kind == MenuKind.FOOD) R.string.menu_title_food else R.string.menu_title_drinks
    val cantinaTitle = viewModel.cantina?.let { "${it.number} — ${it.name}" }.orEmpty()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(stringResource(titleRes))
                        if (cantinaTitle.isNotBlank()) {
                            Text(
                                text = cantinaTitle,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                },
                actions = { LanguageToggle() },
            )
        },
    ) { padding ->
        if (viewModel.items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                Text(stringResource(R.string.menu_empty))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp),
            ) {
                items(viewModel.items, key = { it.id }) { item ->
                    MenuItemCard(
                        item = item,
                        onDescription = { dialogItem = item },
                    )
                }
            }
        }
    }

    dialogItem?.let { item ->
        BilingualDescriptionDialog(
            title = item.name,
            description = item.description,
            onDismiss = { dialogItem = null },
        )
    }
}

@Composable
private fun MenuItemCard(
    item: MenuItem,
    onDescription: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f, fill = false),
                )
                Text(
                    text = stringResource(R.string.menu_available, item.availablePlates),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (item.availablePlates > 0)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.error,
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
            ) {
                AssistChip(
                    onClick = onDescription,
                    label = { Text(stringResource(R.string.action_description)) },
                    leadingIcon = {
                        Icon(Icons.Outlined.Info, contentDescription = null)
                    },
                )
            }
        }
    }
}
