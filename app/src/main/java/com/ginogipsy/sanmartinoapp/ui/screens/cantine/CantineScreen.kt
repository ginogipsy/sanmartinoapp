package com.ginogipsy.sanmartinoapp.ui.screens.cantine

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ginogipsy.sanmartinoapp.R
import com.ginogipsy.sanmartinoapp.data.model.Cantina
import com.ginogipsy.sanmartinoapp.data.model.MenuKind
import com.ginogipsy.sanmartinoapp.data.search.CantinaSearchResult
import com.ginogipsy.sanmartinoapp.ui.AppViewModelProvider
import com.ginogipsy.sanmartinoapp.ui.components.BilingualDescriptionDialog
import com.ginogipsy.sanmartinoapp.ui.components.CantinaNumberBadge
import com.ginogipsy.sanmartinoapp.ui.components.ErrorState
import com.ginogipsy.sanmartinoapp.ui.components.LanguageToggle
import com.ginogipsy.sanmartinoapp.ui.components.LoadingState
import com.ginogipsy.sanmartinoapp.ui.state.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CantineScreen(
    onBack: () -> Unit,
    onOpenMenu: (cantinaId: String, kind: MenuKind) -> Unit,
    viewModel: CantineViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val context = LocalContext.current
    val resultsState by viewModel.results.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    var dialogCantina by remember { mutableStateOf<Cantina?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.cantine_title)) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            SearchField(
                query = query,
                onQueryChange = viewModel::onQueryChange,
                onClear = viewModel::clearQuery,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            )

            when (val state = resultsState) {
                is UiState.Loading -> LoadingState(modifier = Modifier.fillMaxSize())
                is UiState.Error -> ErrorState(
                    message = state.message,
                    onRetry = viewModel::refresh,
                    modifier = Modifier.fillMaxSize(),
                )
                is UiState.Success -> {
                    val results = state.data
                    if (results.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = stringResource(R.string.search_no_results),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(24.dp),
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 16.dp),
                        ) {
                            items(results, key = { it.cantina.id }) { result ->
                                CantinaCard(
                                    result = result,
                                    showMatchCount = query.isNotBlank(),
                                    onDescription = { dialogCantina = result.cantina },
                                    onFood = { onOpenMenu(result.cantina.id, MenuKind.FOOD) },
                                    onDrinks = { onOpenMenu(result.cantina.id, MenuKind.DRINK) },
                                    onLocation = { openMaps(context, result.cantina) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    dialogCantina?.let { cantina ->
        BilingualDescriptionDialog(
            title = "${cantina.number} — ${cantina.name}",
            description = cantina.description,
            onDismiss = { dialogCantina = null },
            extra = {
                Text(
                    text = stringResource(
                        R.string.cantina_first_participation,
                        cantina.firstParticipationYear,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = { Text(stringResource(R.string.search_placeholder)) },
        leadingIcon = {
            Icon(Icons.Filled.Search, contentDescription = null)
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = onClear) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(R.string.search_clear_cd),
                    )
                }
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
    )
}

@Composable
private fun CantinaCard(
    result: CantinaSearchResult,
    showMatchCount: Boolean,
    onDescription: () -> Unit,
    onFood: () -> Unit,
    onDrinks: () -> Unit,
    onLocation: () -> Unit,
) {
    val cantina = result.cantina
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                CantinaNumberBadge(number = cantina.number)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = cantina.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    if (showMatchCount && result.matchCount > 0) {
                        MatchCountBadge(
                            count = result.matchCount,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AssistChip(
                    onClick = onDescription,
                    label = { Text(stringResource(R.string.action_description)) },
                    leadingIcon = {
                        Icon(Icons.Outlined.Info, contentDescription = null)
                    },
                )
                AssistChip(
                    onClick = onLocation,
                    label = { Text(stringResource(R.string.action_location)) },
                    leadingIcon = {
                        Icon(Icons.Filled.Place, contentDescription = null)
                    },
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AssistChip(
                    onClick = onFood,
                    label = { Text(stringResource(R.string.action_food)) },
                    leadingIcon = {
                        Icon(Icons.Filled.MenuBook, contentDescription = null)
                    },
                )
                AssistChip(
                    onClick = onDrinks,
                    label = { Text(stringResource(R.string.action_drinks)) },
                    leadingIcon = {
                        Icon(Icons.Filled.LocalBar, contentDescription = null)
                    },
                )
            }
        }
    }
}

@Composable
private fun MatchCountBadge(count: Int, modifier: Modifier = Modifier) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        shape = MaterialTheme.shapes.small,
        modifier = modifier,
    ) {
        Text(
            text = pluralStringResource(R.plurals.search_match_count, count, count),
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}

private fun openMaps(context: Context, cantina: Cantina) {
    val label = Uri.encode("${cantina.number} - ${cantina.name}")
    val geoUri = Uri.parse("geo:${cantina.latitude},${cantina.longitude}?q=${cantina.latitude},${cantina.longitude}($label)")
    val geoIntent = Intent(Intent.ACTION_VIEW, geoUri).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    if (geoIntent.resolveActivity(context.packageManager) != null) {
        context.startActivity(geoIntent)
        return
    }
    val webUri = Uri.parse(
        "https://www.google.com/maps/search/?api=1&query=${cantina.latitude},${cantina.longitude}"
    )
    context.startActivity(
        Intent(Intent.ACTION_VIEW, webUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    )
}
