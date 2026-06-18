package com.ginogipsy.sanmartinoapp.data.repository

import com.ginogipsy.sanmartinoapp.data.model.Event
import com.ginogipsy.sanmartinoapp.network.NetworkResult
import com.ginogipsy.sanmartinoapp.network.generated.events.api.EventsApi
import com.ginogipsy.sanmartinoapp.network.map
import com.ginogipsy.sanmartinoapp.network.mapper.toDomain
import com.ginogipsy.sanmartinoapp.network.safeApiCall
import java.util.UUID

interface EventRepository {
    suspend fun getEvents(): NetworkResult<List<Event>>
    suspend fun getEvent(id: String): NetworkResult<Event>
}

class RemoteEventRepository(
    private val api: EventsApi,
) : EventRepository {

    override suspend fun getEvents(): NetworkResult<List<Event>> =
        safeApiCall { api.listEvents() }
            .map { dtos -> dtos.map { it.toDomain() } }

    override suspend fun getEvent(id: String): NetworkResult<Event> {
        val uuid = runCatching { UUID.fromString(id) }.getOrElse {
            return NetworkResult.Error.Unknown(
                throwable = it,
                message = "ID evento non valido: $id",
            )
        }
        return safeApiCall { api.getEvent(uuid) }.map { it.toDomain() }
    }
}
