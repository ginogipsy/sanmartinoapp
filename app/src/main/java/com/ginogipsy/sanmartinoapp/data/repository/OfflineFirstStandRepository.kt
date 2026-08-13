package com.ginogipsy.sanmartinoapp.data.repository

import com.ginogipsy.sanmartinoapp.data.local.dao.CantinaDao
import com.ginogipsy.sanmartinoapp.data.local.entity.toDomain
import com.ginogipsy.sanmartinoapp.data.local.entity.toEntity
import com.ginogipsy.sanmartinoapp.data.model.Cantina
import com.ginogipsy.sanmartinoapp.network.NetworkResult
import com.ginogipsy.sanmartinoapp.network.generated.stands.api.StandsApi
import com.ginogipsy.sanmartinoapp.network.map
import com.ginogipsy.sanmartinoapp.network.mapper.toDomain
import com.ginogipsy.sanmartinoapp.network.safeApiCall
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class OfflineFirstStandRepository(
    private val api: StandsApi,
    private val dao: CantinaDao
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

    private suspend fun saveFullCantina(cantina: Cantina) {
        dao.insertCantine(listOf(cantina.toEntity()))
        dao.clearMenuItemsForStand(cantina.id)
        dao.insertMenuItems(cantina.foods.map { it.toEntity(cantina.id) })
        dao.insertMenuItems(cantina.drinks.map { it.toEntity(cantina.id) })
    }
}
