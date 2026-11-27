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
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ginogipsy.sanmartinoapp.R
import com.ginogipsy.sanmartinoapp.data.model.Language
import com.ginogipsy.sanmartinoapp.data.model.MenuItem
import com.ginogipsy.sanmartinoapp.data.model.MenuKind
import com.ginogipsy.sanmartinoapp.data.model.ReviewTargetType
import com.ginogipsy.sanmartinoapp.i18n.LocalLanguage
import com.ginogipsy.sanmartinoapp.i18n.localizedStringResource as stringResource
import com.ginogipsy.sanmartinoapp.ui.components.AccountTopBarAction
import com.ginogipsy.sanmartinoapp.ui.components.BilingualDescriptionDialog
import com.ginogipsy.sanmartinoapp.ui.components.ErrorState
import com.ginogipsy.sanmartinoapp.ui.components.LanguageToggle
import com.ginogipsy.sanmartinoapp.ui.components.LoadingState
import com.ginogipsy.sanmartinoapp.ui.components.ReviewsDialog
import com.ginogipsy.sanmartinoapp.ui.screens.cantine.HistoricalReadOnlyBanner
import com.ginogipsy.sanmartinoapp.ui.state.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    cantinaId: String,
    kind: MenuKind,
    eventId: String = "11111111-1111-1111-1111-000000002026",
    eventName: String = "",
    readOnly: Boolean = false,
    onBack: () -> Unit,
) {
    val viewModel: MenuViewModel = viewModel(
        key = "menu-$cantinaId-${kind.name}-$eventId",
        factory = MenuViewModel.factory(cantinaId, kind, eventId),
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var dialogItem by remember { mutableStateOf<MenuItem?>(null) }
    var reviewsItem by remember { mutableStateOf<MenuItem?>(null) }

    val titleRes = if (kind == MenuKind.FOOD) R.string.menu_title_food else R.string.menu_title_drinks
    val cantinaTitle = (uiState as? UiState.Success<MenuData>)?.data?.cantina
        ?.let { "${it.number} — ${it.name}" }
        .orEmpty()

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        topBar = {
            TopAppBar(
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.ui.graphics.Color.Transparent,
                ),
                title = {
                    Column {
                        com.ginogipsy.sanmartinoapp.ui.components.BrandTitle(stringResource(titleRes))
                        val subtitle = listOfNotNull(
                            cantinaTitle.takeIf { it.isNotBlank() },
                            eventName.takeIf { it.isNotBlank() },
                        ).joinToString(" • ")
                        if (subtitle.isNotBlank()) {
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = androidx.compose.ui.graphics.Color.White,
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

            when (val state = uiState) {
                is UiState.Loading -> LoadingState(
                    modifier = Modifier.fillMaxSize(),
                )
                is UiState.Error -> ErrorState(
                    message = state.message,
                    onRetry = viewModel::refresh,
                    modifier = Modifier.fillMaxSize(),
                )
                is UiState.Success -> {
                    val items = state.data.items
                    if (items.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            androidx.compose.material3.Surface(
                                shape = MaterialTheme.shapes.medium,
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = androidx.compose.ui.graphics.Color.Black,
                                shadowElevation = 2.dp,
                                modifier = Modifier.padding(24.dp),
                            ) {
                                Text(
                                    text = stringResource(R.string.menu_empty),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = androidx.compose.ui.graphics.Color.Black,
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
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp),
                        ) {
                            items(items, key = { it.id }) { item ->
                                MenuItemCard(
                                    item = item,
                                    readOnly = readOnly,
                                    onClick = {
                                        viewModel.onMenuItemClicked(item)
                                        dialogItem = item
                                    },
                                    onReviews = {
                                        viewModel.onMenuItemClicked(item)
                                        reviewsItem = item
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    dialogItem?.let { item ->
        val lang = LocalLanguage.current.value
        val desc = item.description.pick(lang)
        val title = if (lang == Language.EN && desc.isNotBlank()) desc else item.name
        BilingualDescriptionDialog(
            title = title,
            description = item.description,
            onDismiss = { dialogItem = null },
            extra = {
                Text(
                    text = if (readOnly)
                        stringResource(R.string.menu_historical_recipe)
                    else
                        stringResource(R.string.menu_available, item.availablePlates),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
        )
    }

    reviewsItem?.let { item ->
        ReviewsDialog(
            targetType = ReviewTargetType.MENU_ITEM,
            targetId = item.id,
            standId = cantinaId,
            targetTitle = if (cantinaTitle.isNotBlank()) "${item.name} ($cantinaTitle)" else item.name,
            readOnly = readOnly,
            onDismiss = { reviewsItem = null },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MenuItemCard(
    item: MenuItem,
    readOnly: Boolean,
    onClick: () -> Unit,
    onReviews: () -> Unit,
) {
    val lang = LocalLanguage.current.value
    val itemDesc = item.description.pick(lang)
    val displayName = if (lang == Language.EN && itemDesc.isNotBlank()) itemDesc else item.name
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (readOnly) {
                    Text(
                        text = stringResource(R.string.menu_historical_recipe),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    Text(
                        text = stringResource(R.string.menu_available, item.availablePlates),
                        style = MaterialTheme.typography.labelLarge,
                        color = if (item.availablePlates > 0)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.error,
                    )
                }
            }
            if (itemDesc.isNotBlank() && itemDesc != displayName) {
                Text(
                    text = itemDesc,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                AssistChip(
                    onClick = onReviews,
                    label = { Text(stringResource(R.string.action_reviews)) },
                    leadingIcon = {
                        Icon(Icons.Filled.Star, contentDescription = null)
                    },
                )
            }
        }
    }
}
