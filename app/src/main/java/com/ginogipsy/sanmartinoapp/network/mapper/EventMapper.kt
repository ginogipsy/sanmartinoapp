package com.ginogipsy.sanmartinoapp.network.mapper

import com.ginogipsy.sanmartinoapp.data.model.Event
import com.ginogipsy.sanmartinoapp.data.model.LocalizedText
import com.ginogipsy.sanmartinoapp.network.generated.events.model.EventDto
import com.ginogipsy.sanmartinoapp.network.generated.events.model.LocalizedTextDto

/**
 * DTO -> modello UI per gli eventi.
 *
 * Nota: ignoriamo deliberatamente `EventDto.status`. Lo stato (UPCOMING / PAST) viene
 * derivato lato client da `Event.isUpcoming(LocalDate.now())`, cosi' una card cacheata
 * offline non resta "UPCOMING" all'infinito se il dispositivo non sincronizza per giorni.
 */
fun EventDto.toDomain(): Event = Event(
    id = id.toString(),
    name = name,
    place = place,
    startDate = startDate,
    endDate = endDate,
    description = description.toDomain(),
)

internal fun LocalizedTextDto.toDomain(): LocalizedText =
    LocalizedText(it = `it`, en = en)
