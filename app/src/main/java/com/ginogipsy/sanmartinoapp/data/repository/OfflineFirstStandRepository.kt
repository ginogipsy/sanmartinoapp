package com.ginogipsy.sanmartinoapp.data.repository

import com.ginogipsy.sanmartinoapp.data.local.dao.CantinaDao
import com.ginogipsy.sanmartinoapp.data.local.entity.toDomain
import com.ginogipsy.sanmartinoapp.data.local.entity.toEntity
import com.ginogipsy.sanmartinoapp.data.model.Cantina
import com.ginogipsy.sanmartinoapp.data.model.FestivalReview
import com.ginogipsy.sanmartinoapp.data.model.MenuKind
import com.ginogipsy.sanmartinoapp.data.model.ReviewStatus
import com.ginogipsy.sanmartinoapp.data.model.ReviewTargetType
import com.ginogipsy.sanmartinoapp.data.model.StandInteractionType
import com.ginogipsy.sanmartinoapp.data.search.CantinaSearchResult
import com.ginogipsy.sanmartinoapp.network.NetworkResult
import com.ginogipsy.sanmartinoapp.network.generated.stands.api.AnalyticsApi
import com.ginogipsy.sanmartinoapp.network.generated.stands.api.ReviewsApi
import com.ginogipsy.sanmartinoapp.network.generated.stands.api.StandsApi
import com.ginogipsy.sanmartinoapp.network.generated.stands.model.FestivalReviewCreateDto
import com.ginogipsy.sanmartinoapp.network.generated.stands.model.ReviewStatusDto
import com.ginogipsy.sanmartinoapp.network.generated.stands.model.ReviewTargetTypeDto
import com.ginogipsy.sanmartinoapp.network.generated.stands.model.StandInteractionCreateDto
import com.ginogipsy.sanmartinoapp.network.generated.stands.model.StandInteractionTypeDto
import com.ginogipsy.sanmartinoapp.network.map
import com.ginogipsy.sanmartinoapp.network.mapper.toDomain
import com.ginogipsy.sanmartinoapp.network.mapper.toDto
import com.ginogipsy.sanmartinoapp.network.safeApiCall
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class OfflineFirstStandRepository(
    private val api: StandsApi,
    private val dao: CantinaDao,
    private val analyticsApi: AnalyticsApi? = null,
    private val reviewsApi: ReviewsApi? = null,
) : StandRepository {

    override fun getStandsStream(): Flow<List<Cantina>> =
        dao.getAllCantine().map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun refreshStands(): NetworkResult<Unit> {
        val result = safeApiCall { api.listStands() }
        if (result is NetworkResult.Success) {
            val entities = result.data.map { it.toDomain().toEntity() }
            dao.insertCantine(entities)

            coroutineScope {
                result.data.map { summary ->
                    async { safeApiCall { api.getStand(summary.id) } }
                }.awaitAll().forEach { detailResult ->
                    if (detailResult is NetworkResult.Success) {
                        saveFullCantina(detailResult.data.toDomain())
                    }
                }
            }
        }
        return result.map { }
    }

    override suspend fun getStand(id: String): NetworkResult<Cantina> {
        val uuid = runCatching { UUID.fromString(id) }.getOrElse {
            return NetworkResult.Error.Unknown(throwable = it, message = "ID non valido")
        }
        val result = safeApiCall { api.getStand(uuid) }
        if (result is NetworkResult.Success) {
            val cantina = result.data.toDomain()
            saveFullCantina(cantina)
        }
        return result.map { it.toDomain() }
    }

    override fun getStandStream(id: String): Flow<Cantina?> = flow {
        val entity = dao.getCantinaById(id)
        emit(entity?.toDomain())
    }

    override suspend fun searchStands(
        query: String,
        kind: MenuKind?,
        onlyAvailable: Boolean,
    ): NetworkResult<List<CantinaSearchResult>> {
        val result = safeApiCall {
            api.searchStands(
                q = query,
                kind = kind?.toDto(),
                onlyAvailable = onlyAvailable,
            )
        }
        return result.map { list ->
            list.map { dto -> dto.toDomain() }
        }
    }

    override suspend fun recordInteraction(
        standId: String,
        interactionType: StandInteractionType,
        menuItemId: String?,
        searchQuery: String?,
    ): NetworkResult<Unit> {
        val client = analyticsApi ?: return NetworkResult.Success(Unit)
        val standUuid = runCatching { UUID.fromString(standId) }.getOrElse {
            return NetworkResult.Error.Unknown(throwable = it, message = "standId non valido")
        }
        val itemUuid = menuItemId?.let {
            runCatching { UUID.fromString(it) }.getOrNull()
        }
        val dto = StandInteractionCreateDto(
            standId = standUuid,
            interactionType = StandInteractionTypeDto.valueOf(interactionType.name),
            menuItemId = itemUuid,
            searchQuery = searchQuery?.takeIf { it.isNotBlank() }?.take(200),
        )
        return safeApiCall { client.recordInteraction(dto) }
    }

    override suspend fun listReviews(
        targetType: ReviewTargetType,
        targetId: String,
        status: ReviewStatus?,
    ): NetworkResult<List<FestivalReview>> {
        val client = reviewsApi ?: return NetworkResult.Success(emptyList())
        val targetUuid = runCatching { UUID.fromString(targetId) }.getOrElse {
            return NetworkResult.Error.Unknown(throwable = it, message = "targetId non valido")
        }
        val result = safeApiCall {
            client.listReviews(
                targetType = ReviewTargetTypeDto.valueOf(targetType.name),
                targetId = targetUuid,
                status = status?.let { ReviewStatusDto.valueOf(it.name) },
            )
        }
        return result.map { list -> list.map { it.toDomain() } }
    }

    override suspend fun createReview(
        targetType: ReviewTargetType,
        targetId: String,
        standId: String?,
        targetName: String?,
        authorUsername: String?,
        rating: Int,
        comment: String,
    ): NetworkResult<FestivalReview> {
        val client = reviewsApi ?: return NetworkResult.Error.Unknown(
            throwable = IllegalStateException("ReviewsApi non configurata"),
            message = "Servizio recensioni non disponibile",
        )
        val targetUuid = runCatching { UUID.fromString(targetId) }.getOrElse {
            return NetworkResult.Error.Unknown(throwable = it, message = "targetId non valido")
        }
        val standUuid = standId?.let { runCatching { UUID.fromString(it) }.getOrNull() }
        val dto = FestivalReviewCreateDto(
            targetType = ReviewTargetTypeDto.valueOf(targetType.name),
            targetId = targetUuid,
            rating = rating.coerceIn(1, 5),
            comment = comment.trim(),
            standId = standUuid,
            targetName = targetName?.take(200),
            authorUsername = authorUsername?.take(120),
        )
        return safeApiCall { client.createReview(dto) }.map { it.toDomain() }
    }

    private suspend fun saveFullCantina(cantina: Cantina) {
        dao.insertCantine(listOf(cantina.toEntity()))
        dao.clearMenuItemsForStand(cantina.id)
        dao.insertMenuItems(cantina.foods.map { it.toEntity(cantina.id) })
        dao.insertMenuItems(cantina.drinks.map { it.toEntity(cantina.id) })
    }
}
