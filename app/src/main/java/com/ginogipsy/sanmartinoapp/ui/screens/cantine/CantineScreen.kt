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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ginogipsy.sanmartinoapp.R
import com.ginogipsy.sanmartinoapp.data.model.Cantina
import com.ginogipsy.sanmartinoapp.data.model.Language
import com.ginogipsy.sanmartinoapp.data.model.MenuKind
import com.ginogipsy.sanmartinoapp.data.model.ReviewTargetType
import com.ginogipsy.sanmartinoapp.data.model.StandInteractionType
import com.ginogipsy.sanmartinoapp.data.search.CantinaSearchResult
import com.ginogipsy.sanmartinoapp.i18n.LocalLanguage
import androidx.compose.ui.res.pluralStringResource
import com.ginogipsy.sanmartinoapp.i18n.localizedStringResource as stringResource
import com.ginogipsy.sanmartinoapp.ui.components.AccountTopBarAction
import com.ginogipsy.sanmartinoapp.ui.components.BilingualDescriptionDialog
import com.ginogipsy.sanmartinoapp.ui.components.CantinaNumberBadge
import com.ginogipsy.sanmartinoapp.ui.components.ErrorState
import com.ginogipsy.sanmartinoapp.ui.components.LanguageToggle
import com.ginogipsy.sanmartinoapp.ui.components.LoadingState
import com.ginogipsy.sanmartinoapp.ui.components.ReviewsDialog
import com.ginogipsy.sanmartinoapp.ui.state.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CantineScreen(
    eventId: String = "11111111-1111-1111-1111-000000002026",
    eventName: String = "",
    readOnly: Boolean = false,
    onBack: () -> Unit,
    onOpenMenu: (cantinaId: String, kind: MenuKind) -> Unit,
    viewModel: CantineViewModel = viewModel(
        key = "cantine-$eventId",
        factory = CantineViewModel.factory(eventId),
    ),
) {
    val context = LocalContext.current
    val resultsState by viewModel.results.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val onlyFavorites by viewModel.onlyFavorites.collectAsStateWithLifecycle()
    val favoriteStandIds by viewModel.favoriteStandIds.collectAsStateWithLifecycle()
    var dialogCantina by remember { mutableStateOf<Cantina?>(null) }
    var reviewsCantina by remember { mutableStateOf<Cantina?>(null) }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                ),
                title = {
                    Column {
                        com.ginogipsy.sanmartinoapp.ui.components.BrandTitle(stringResource(R.string.cantine_title))
                        if (eventName.isNotBlank()) {
                            Text(
                                text = eventName,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White,
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
                actions = {
                    AccountTopBarAction()
                    LanguageToggle()
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            if (readOnly) {
                HistoricalReadOnlyBanner(
                    eventName = eventName,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                )
            }

            SearchField(
                query = query,
                onQueryChange = viewModel::onQueryChange,
                onClear = viewModel::clearQuery,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = !onlyFavorites,
                    onClick = { viewModel.setOnlyFavorites(false) },
                    label = { Text(stringResource(R.string.favorites_filter_all)) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                )
                FilterChip(
                    selected = onlyFavorites,
                    onClick = { viewModel.toggleOnlyFavorites() },
                    label = {
                        Text(
                            stringResource(
                                R.string.favorites_filter_only,
                                favoriteStandIds.size,
                            )
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = if (onlyFavorites) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = null,
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                )
            }

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
                            Surface(
                                shape = MaterialTheme.shapes.medium,
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = Color.Black,
                                shadowElevation = 2.dp,
                                modifier = Modifier.padding(24.dp),
                            ) {
                                Text(
                                    text = if (onlyFavorites && query.isBlank())
                                        stringResource(R.string.favorites_empty)
                                    else
                                        stringResource(R.string.search_no_results),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Black,
                                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                                )
                            }
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
                                    readOnly = readOnly,
                                    isFavorite = favoriteStandIds.contains(result.cantina.id),
                                    onToggleFavorite = {
                                        viewModel.toggleFavorite(result.cantina.id)
                                    },
                                    onDescription = {
                                        viewModel.recordStandInteraction(
                                            result.cantina.id,
                                            StandInteractionType.STAND_DESCRIPTION_OPEN,
                                        )
                                        dialogCantina = result.cantina
                                    },
                                    onFood = {
                                        viewModel.recordStandInteraction(
                                            result.cantina.id,
                                            StandInteractionType.STAND_FOOD_OPEN,
                                        )
                                        onOpenMenu(result.cantina.id, MenuKind.FOOD)
                                    },
                                    onDrinks = {
                                        viewModel.recordStandInteraction(
                                            result.cantina.id,
                                            StandInteractionType.STAND_DRINK_OPEN,
                                        )
                                        onOpenMenu(result.cantina.id, MenuKind.DRINK)
                                    },
                                    onLocation = {
                                        viewModel.recordStandInteraction(
                                            result.cantina.id,
                                            StandInteractionType.STAND_MAP_OPEN,
                                        )
                                        openMaps(context, result.cantina)
                                    },
                                    onReviews = {
                                        reviewsCantina = result.cantina
                                    },
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

    reviewsCantina?.let { cantina ->
        ReviewsDialog(
            targetType = ReviewTargetType.STAND,
            targetId = cantina.id,
            standId = cantina.id,
            targetTitle = "${cantina.number} — ${cantina.name}",
            readOnly = readOnly,
            onDismiss = { reviewsCantina = null },
        )
    }
}

@Composable
internal fun HistoricalReadOnlyBanner(
    eventName: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        shape = MaterialTheme.shapes.medium,
        shadowElevation = 2.dp,
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.History,
                contentDescription = null,
            )
            Column {
                Text(
                    text = if (eventName.isNotBlank())
                        "${stringResource(R.string.archive_readonly_banner_title)} • $eventName"
                    else
                        stringResource(R.string.archive_readonly_banner_title),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = stringResource(R.string.archive_readonly_banner_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
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
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            disabledContainerColor = Color.White,
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black,
            focusedPlaceholderColor = Color.DarkGray,
            unfocusedPlaceholderColor = Color.DarkGray,
            focusedLeadingIconColor = Color.Black,
            unfocusedLeadingIconColor = Color.DarkGray,
            focusedTrailingIconColor = Color.Black,
            unfocusedTrailingIconColor = Color.DarkGray,
            cursorColor = Color.Black,
        ),
    )
}

@Composable
private fun CantinaCard(
    result: CantinaSearchResult,
    showMatchCount: Boolean,
    readOnly: Boolean,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onDescription: () -> Unit,
    onFood: () -> Unit,
    onDrinks: () -> Unit,
    onLocation: () -> Unit,
    onReviews: () -> Unit,
) {
    val cantina = result.cantina
    val lang = LocalLanguage.current.value
    val descriptionText = cantina.description.pick(lang)
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
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
                    if (!readOnly && (cantina.activeOrdersCount > 0 || cantina.currentServingOrderNumber > 0)) {
                        val queueText = if (lang == Language.EN) {
                            buildString {
                                if (cantina.currentServingOrderNumber > 0) {
                                    append("Serving #${cantina.currentServingOrderNumber.toString().padStart(4, '0')}")
                                }
                                if (cantina.activeOrdersCount > 0) {
                                    if (isNotEmpty()) append(" • ")
                                    append("Queue: ${cantina.activeOrdersCount} (~${cantina.estimatedWaitMinutes} min)")
                                }
                            }
                        } else {
                            buildString {
                                if (cantina.currentServingOrderNumber > 0) {
                                    append("In uscita #${cantina.currentServingOrderNumber.toString().padStart(4, '0')}")
                                }
                                if (cantina.activeOrdersCount > 0) {
                                    if (isNotEmpty()) append(" • ")
                                    append("In coda: ${cantina.activeOrdersCount} (~${cantina.estimatedWaitMinutes} min)")
                                }
                            }
                        }
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.padding(top = 4.dp),
                        ) {
                            Text(
                                text = "⚡ $queueText",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            )
                        }
                    }
                }
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = stringResource(
                            if (isFavorite) R.string.favorites_remove_cd else R.string.favorites_add_cd
                        ),
                        tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (descriptionText.isNotBlank()) {
                Text(
                    text = descriptionText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }

            val foodsToDisplay = if (result.matchingFoods.isNotEmpty()) result.matchingFoods else cantina.foods
            if (foodsToDisplay.isNotEmpty()) {
                val foodNames = foodsToDisplay.joinToString(", ") { item ->
                    val desc = item.description.pick(lang)
                    if (lang == Language.EN && desc.isNotBlank()) desc else item.name
                }
                Text(
                    text = stringResource(R.string.action_food) + ": " + foodNames,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 6.dp),
                )
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
                AssistChip(
                    onClick = onReviews,
                    label = { Text(stringResource(R.string.action_reviews)) },
                    leadingIcon = {
                        Icon(Icons.Filled.Star, contentDescription = null)
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
