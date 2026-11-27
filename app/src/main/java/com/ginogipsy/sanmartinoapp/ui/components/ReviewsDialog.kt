package com.ginogipsy.sanmartinoapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.ginogipsy.sanmartinoapp.i18n.localizedStringResource as stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ginogipsy.sanmartinoapp.R
import com.ginogipsy.sanmartinoapp.SanMartinoApplication
import com.ginogipsy.sanmartinoapp.data.model.FestivalReview
import com.ginogipsy.sanmartinoapp.data.model.ReviewStatus
import com.ginogipsy.sanmartinoapp.data.model.ReviewTargetType
import com.ginogipsy.sanmartinoapp.network.NetworkResult
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import java.util.Locale

private val reviewDateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", Locale.ITALY)

@Composable
fun ReviewsDialog(
    targetType: ReviewTargetType,
    targetId: String,
    targetTitle: String,
    standId: String? = null,
    readOnly: Boolean = false,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val container = remember(context) {
        (context.applicationContext as SanMartinoApplication).container
    }
    val authRepository = container.authRepository
    val standRepository = container.standRepository
    val currentUser by authRepository.currentUser.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()
    var reviews by remember { mutableStateOf<List<FestivalReview>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedRating by remember { mutableIntStateOf(5) }
    var comment by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var feedbackMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showLoginDialog by remember { mutableStateOf(false) }

    LaunchedEffect(targetType, targetId) {
        isLoading = true
        when (val res = standRepository.listReviews(targetType, targetId, ReviewStatus.APPROVED)) {
            is NetworkResult.Success -> reviews = res.data
            is NetworkResult.Error -> reviews = emptyList()
        }
        isLoading = false
    }

    val avgRating = remember(reviews) {
        if (reviews.isEmpty()) 0.0 else reviews.map { it.rating }.average()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = stringResource(R.string.reviews_dialog_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = targetTitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (reviews.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(top = 4.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp),
                        )
                        Text(
                            text = String.format(Locale.ITALY, "%.1f / 5 (%d)", avgRating, reviews.size),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (isLoading) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(28.dp))
                    }
                } else if (reviews.isEmpty()) {
                    Text(
                        text = stringResource(R.string.reviews_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 220.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(reviews, key = { it.id }) { rev ->
                            ReviewItemCard(rev)
                        }
                    }
                }

                HorizontalDivider()

                if (readOnly) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.History,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                            Text(
                                text = stringResource(R.string.reviews_readonly_notice),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                } else if (currentUser == null) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.reviews_login_prompt),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        OutlinedButton(
                            onClick = { showLoginDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(stringResource(R.string.auth_action_login))
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.reviews_write_header, currentUser!!.displayName),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            for (star in 1..5) {
                                IconButton(
                                    onClick = { selectedRating = star },
                                    modifier = Modifier.size(34.dp),
                                ) {
                                    Icon(
                                        imageVector = if (star <= selectedRating)
                                            Icons.Filled.Star
                                        else
                                            Icons.Outlined.StarBorder,
                                        contentDescription = "$star",
                                        tint = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            }
                        }
                        OutlinedTextField(
                            value = comment,
                            onValueChange = {
                                comment = it
                                errorMessage = null
                                feedbackMessage = null
                            },
                            label = { Text(stringResource(R.string.reviews_comment_label)) },
                            minLines = 2,
                            maxLines = 4,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        feedbackMessage?.let { msg ->
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                shape = MaterialTheme.shapes.small,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(
                                    text = msg,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(10.dp),
                                )
                            }
                        }
                        errorMessage?.let { err ->
                            Text(
                                text = err,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (!readOnly && currentUser != null) {
                val submittedMessage = stringResource(R.string.reviews_submitted_pending)
                Button(
                    enabled = !isSubmitting && comment.trim().length >= 2,
                    onClick = {
                        isSubmitting = true
                        errorMessage = null
                        feedbackMessage = null
                        scope.launch {
                            val res = standRepository.createReview(
                                targetType = targetType,
                                targetId = targetId,
                                standId = standId,
                                targetName = targetTitle,
                                authorUsername = currentUser?.username,
                                rating = selectedRating,
                                comment = comment.trim(),
                            )
                            isSubmitting = false
                            when (res) {
                                is NetworkResult.Success -> {
                                    comment = ""
                                    feedbackMessage = submittedMessage
                                }
                                is NetworkResult.Error -> {
                                    errorMessage = res.message ?: "Impossibile inviare la recensione"
                                }
                            }
                        }
                    },
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text(stringResource(R.string.reviews_action_submit))
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_close))
            }
        },
    )

    if (showLoginDialog) {
        AuthAccountDialog(
            authRepository = authRepository,
            currentUser = currentUser,
            onDismiss = { showLoginDialog = false },
        )
    }
}

@Composable
private fun ReviewItemCard(review: FestivalReview) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "@${review.authorUsername}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    repeat(review.rating.coerceIn(1, 5)) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp),
                        )
                    }
                }
            }
            Text(
                text = review.comment,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp),
            )
            Text(
                text = runCatching { review.createdAt.format(reviewDateFormatter) }.getOrDefault(""),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}
