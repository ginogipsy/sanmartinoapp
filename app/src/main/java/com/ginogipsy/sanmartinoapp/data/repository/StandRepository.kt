package com.ginogipsy.sanmartinoapp.data.repository

import com.ginogipsy.sanmartinoapp.data.model.Cantina
import com.ginogipsy.sanmartinoapp.network.NetworkResult
import com.ginogipsy.sanmartinoapp.network.generated.stands.api.StandsApi
import com.ginogipsy.sanmartinoapp.network.map
import com.ginogipsy.sanmartinoapp.network.mapper.toDomain
import com.ginogipsy.sanmartinoapp.network.safeApiCall
import java.util.UUID

interface StandRepository {
    /** Lista delle cantine (summary, senza menu/owners). */
    suspend fun getStands(): NetworkResult<List<Cantina>>

    /** Dettaglio singola cantina, comprensivo di menu (food + drinks). */
    suspend fun getStand(id: String): NetworkResult<Cantina>
}

class RemoteStandRepository(
    private val api: StandsApi,
) : StandRepository {

    override suspend fun getStands(): NetworkResult<List<Cantina>> =
        safeApiCall { api.listStands() }
            .map { dtos -> dtos.map { it.toDomain() } }

    override suspend fun getStand(id: String): NetworkResult<Cantina> {
        val uuid = runCatching { UUID.fromString(id) }.getOrElse {
            return NetworkResult.Error.Unknown(
                throwable = it,
                message = "ID cantina non valido: $id",
            )
        }
        return safeApiCall { api.getStand(uuid) }.map { it.toDomain() }
    }
}
