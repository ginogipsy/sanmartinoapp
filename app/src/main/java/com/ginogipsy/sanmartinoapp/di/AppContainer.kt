package com.ginogipsy.sanmartinoapp.di

import android.content.Context
import androidx.room.Room
import com.ginogipsy.sanmartinoapp.data.local.AppDatabase
import com.ginogipsy.sanmartinoapp.data.repository.AuthRepository
import com.ginogipsy.sanmartinoapp.data.repository.DefaultAuthRepository
import com.ginogipsy.sanmartinoapp.data.repository.DefaultFavoritesRepository
import com.ginogipsy.sanmartinoapp.data.repository.EventRepository
import com.ginogipsy.sanmartinoapp.data.repository.FavoritesRepository
import com.ginogipsy.sanmartinoapp.data.repository.OfflineFirstStandRepository
import com.ginogipsy.sanmartinoapp.data.repository.RemoteEventRepository
import com.ginogipsy.sanmartinoapp.data.repository.StandRepository
import com.ginogipsy.sanmartinoapp.network.HttpClient
import com.ginogipsy.sanmartinoapp.network.auth.AuthApi
import com.ginogipsy.sanmartinoapp.network.generated.events.api.EventsApi
import com.ginogipsy.sanmartinoapp.network.generated.stands.api.AnalyticsApi
import com.ginogipsy.sanmartinoapp.network.generated.stands.api.FavoritesApi
import com.ginogipsy.sanmartinoapp.network.generated.stands.api.ReviewsApi
import com.ginogipsy.sanmartinoapp.network.generated.stands.api.StandsApi
import retrofit2.Retrofit

/**
 * Composition root applicativo.
 */
interface AppContainer {
    val authRepository: AuthRepository
    val favoritesRepository: FavoritesRepository
    val eventRepository: EventRepository
    val standRepository: StandRepository
}

class DefaultAppContainer(private val context: Context) : AppContainer {

    override val authRepository: AuthRepository by lazy {
        DefaultAuthRepository(context) { authApi }
    }

    override val favoritesRepository: FavoritesRepository by lazy {
        DefaultFavoritesRepository(context, authRepository) { favoritesApi }
    }

    private val retrofit: Retrofit by lazy {
        HttpClient.create(tokenProvider = authRepository)
    }

    private val authApi: AuthApi by lazy {
        retrofit.create(AuthApi::class.java)
    }

    private val eventsApi: EventsApi by lazy {
        retrofit.create(EventsApi::class.java)
    }

    private val standsApi: StandsApi by lazy {
        retrofit.create(StandsApi::class.java)
    }

    private val analyticsApi: AnalyticsApi by lazy {
        retrofit.create(AnalyticsApi::class.java)
    }

    private val reviewsApi: ReviewsApi by lazy {
        retrofit.create(ReviewsApi::class.java)
    }

    private val favoritesApi: FavoritesApi by lazy {
        retrofit.create(FavoritesApi::class.java)
    }

    private val database: AppDatabase by lazy {
        Room.databaseBuilder(context, AppDatabase::class.java, "san_martino_db")
            .fallbackToDestructiveMigration()
            .build()
    }

    override val eventRepository: EventRepository by lazy {
        RemoteEventRepository(eventsApi)
    }

    override val standRepository: StandRepository by lazy {
        OfflineFirstStandRepository(standsApi, database.cantinaDao(), analyticsApi, reviewsApi)
    }
}
