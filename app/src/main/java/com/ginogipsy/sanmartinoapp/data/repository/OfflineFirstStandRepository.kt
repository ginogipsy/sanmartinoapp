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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class OfflineFirstStandRepository(
    private val api: StandsApi,
    private val dao: CantinaDao,
    private val analyticsApi: AnalyticsApi? = null,
    private val reviewsApi: ReviewsApi? = null,
) : StandRepository {

    companion object {
        const val DEFAULT_EVENT_ID = "11111111-1111-1111-1111-000000002026"
    }

    private val editionCache = MutableStateFlow<Map<String, List<Cantina>>>(emptyMap())

    private fun isDefaultEvent(eventId: String?): Boolean =
        eventId.isNullOrBlank() || eventId == DEFAULT_EVENT_ID

    private fun parseEventUuid(eventId: String?): UUID? =
        eventId?.takeIf { it.isNotBlank() }?.let { runCatching { UUID.fromString(it) }.getOrNull() }

    override fun getStandsStream(eventId: String?): Flow<List<Cantina>> {
        if (isDefaultEvent(eventId)) {
            return dao.getAllCantine().map { entities ->
                entities.map { it.toDomain() }
            }
        }
        val key = eventId!!.trim()
        return editionCache.map { map -> map[key] ?: emptyList() }
    }

    override suspend fun refreshStands(eventId: String?): NetworkResult<Unit> {
        val eventUuid = parseEventUuid(eventId)
        val result = safeApiCall { api.listStands(eventId = eventUuid) }
        if (result is NetworkResult.Success) {
            if (isDefaultEvent(eventId)) {
                val entities = result.data.map { it.toDomain().toEntity() }
                dao.insertCantine(entities)
            }

            val detailedCantine = mutableListOf<Cantina>()
            coroutineScope {
                result.data.map { summary ->
                    async { safeApiCall { api.getStand(id = summary.id, eventId = eventUuid) } }
                }.awaitAll().forEachIndexed { idx, detailResult ->
                    if (detailResult is NetworkResult.Success) {
                        val full = detailResult.data.toDomain()
                        detailedCantine.add(full)
                        if (isDefaultEvent(eventId)) {
                            saveFullCantina(full)
                        }
                    } else {
                        detailedCantine.add(result.data[idx].toDomain())
                    }
                }
            }
            if (!isDefaultEvent(eventId)) {
                val key = eventId!!.trim()
                editionCache.value = editionCache.value + (key to detailedCantine.sortedBy { it.number })
            }
        }
        return result.map { }
    }

    override suspend fun getStand(id: String, eventId: String?): NetworkResult<Cantina> {
        val uuid = runCatching { UUID.fromString(id) }.getOrElse {
            return NetworkResult.Error.Unknown(throwable = it, message = "ID non valido")
        }
        val eventUuid = parseEventUuid(eventId)
        val result = safeApiCall { api.getStand(id = uuid, eventId = eventUuid) }
        if (result is NetworkResult.Success) {
            val cantina = result.data.toDomain()
            if (isDefaultEvent(eventId)) {
                saveFullCantina(cantina)
            } else {
                val key = eventId!!.trim()
                val current = editionCache.value[key].orEmpty().filterNot { it.id == cantina.id }
                editionCache.value = editionCache.value + (key to (current + cantina).sortedBy { it.number })
            }
        }
        return result.map { it.toDomain() }
    }

    override fun getStandStream(id: String, eventId: String?): Flow<Cantina?> {
        if (isDefaultEvent(eventId)) {
            return flow {
                val entity = dao.getCantinaById(id)
                emit(entity?.toDomain())
            }
        }
        val key = eventId!!.trim()
        return editionCache.map { map ->
            map[key]?.firstOrNull { it.id == id }
        }
    }

    override suspend fun searchStands(
        query: String,
        kind: MenuKind?,
        onlyAvailable: Boolean,
        eventId: String?,
    ): NetworkResult<List<CantinaSearchResult>> {
        val eventUuid = parseEventUuid(eventId)
        val result = safeApiCall {
            api.searchStands(
                q = query,
                kind = kind?.toDto(),
                onlyAvailable = onlyAvailable,
                eventId = eventUuid,
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
