package com.ginogipsy.sanmartinoapp.di

import com.ginogipsy.sanmartinoapp.data.repository.EventRepository
import com.ginogipsy.sanmartinoapp.data.repository.RemoteEventRepository
import com.ginogipsy.sanmartinoapp.data.repository.RemoteStandRepository
import com.ginogipsy.sanmartinoapp.data.repository.StandRepository
import com.ginogipsy.sanmartinoapp.network.HttpClient
import com.ginogipsy.sanmartinoapp.network.generated.events.api.EventsApi
import com.ginogipsy.sanmartinoapp.network.generated.stands.api.StandsApi
import retrofit2.Retrofit

/**
 * Composition root applicativo. Volutamente minimale: niente libreria DI finche' il
 * grafo dipendenze non cresce. Se in futuro arrivano modulo offline/Room, worker,
 * Keycloak, e l'oggetto diventa ingestibile, si valutera' Hilt.
 */
interface AppContainer {
    val eventRepository: EventRepository
    val standRepository: StandRepository
}

class DefaultAppContainer : AppContainer {

    private val retrofit: Retrofit by lazy { HttpClient.create() }

    private val eventsApi: EventsApi by lazy {
        retrofit.create(EventsApi::class.java)
    }

    private val standsApi: StandsApi by lazy {
        retrofit.create(StandsApi::class.java)
    }

    override val eventRepository: EventRepository by lazy {
        RemoteEventRepository(eventsApi)
    }

    override val standRepository: StandRepository by lazy {
        RemoteStandRepository(standsApi)
    }
}
