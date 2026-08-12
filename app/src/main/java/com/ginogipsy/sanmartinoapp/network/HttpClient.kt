package com.ginogipsy.sanmartinoapp.network

import com.ginogipsy.sanmartinoapp.BuildConfig
import com.ginogipsy.sanmartinoapp.network.auth.AnonymousTokenProvider
import com.ginogipsy.sanmartinoapp.network.auth.AuthInterceptor
import com.ginogipsy.sanmartinoapp.network.auth.TokenProvider
import com.ginogipsy.sanmartinoapp.network.serialization.LocalDateSerializer
import com.ginogipsy.sanmartinoapp.network.serialization.LocalDateTimeSerializer
import com.ginogipsy.sanmartinoapp.network.serialization.OffsetDateTimeSerializer
import com.ginogipsy.sanmartinoapp.network.serialization.UUIDSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Factory centralizzata per il client HTTP verso lo Spring Cloud Gateway.
 *
 * Timeout 10s lato client > 5s del gateway (Resilience4j): cosi' gli errori
 * di timeout arrivano dal gateway (utili da loggare/distinguere) invece che
 * dal client.
 */
object HttpClient {

    private val json: Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        serializersModule = SerializersModule {
            contextual(UUIDSerializer)
            contextual(LocalDateSerializer)
            contextual(LocalDateTimeSerializer)
            contextual(OffsetDateTimeSerializer)
        }
    }

    fun create(
        baseUrl: String = BuildConfig.API_BASE_URL,
        tokenProvider: TokenProvider = AnonymousTokenProvider,
    ): Retrofit {
        val ok = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .addInterceptor(AuthInterceptor(tokenProvider))
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(
                        HttpLoggingInterceptor().apply {
                            level = HttpLoggingInterceptor.Level.BASIC
                        }
                    )
                }
            }
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(ok)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }
}
