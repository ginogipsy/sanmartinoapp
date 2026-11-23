package com.ginogipsy.sanmartinoapp.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.ginogipsy.sanmartinoapp.network.NetworkResult
import com.ginogipsy.sanmartinoapp.network.generated.stands.api.FavoritesApi
import com.ginogipsy.sanmartinoapp.network.safeApiCall
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/**
 * Gestisce le cantine preferite ("Dove voglio andare") dell'utente con:
 * 1. Persistenza locale immediata su SharedPreferences (funzionante anche offline o da ospite).
 * 2. Sincronizzazione bidirezionale con `stands-service` (`/v1/stands/favorites`) quando l'utente è autenticato.
 */
interface FavoritesRepository {
    val favoriteStandIds: StateFlow<Set<String>>

    suspend fun toggleFavorite(standId: String)

    suspend fun syncWithRemote()
}

class DefaultFavoritesRepository(
    context: Context,
    private val authRepository: AuthRepository,
    private val favoritesApiProvider: () -> FavoritesApi,
) : FavoritesRepository {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _favoriteStandIds = MutableStateFlow(loadLocalFavorites())
    override val favoriteStandIds: StateFlow<Set<String>> = _favoriteStandIds.asStateFlow()

    override suspend fun toggleFavorite(standId: String) {
        val current = _favoriteStandIds.value
        val isAdding = !current.contains(standId)
        val updated = if (isAdding) current + standId else current - standId
        saveLocalFavorites(updated)

        if (authRepository.currentToken() != null) {
            val uuid = runCatching { UUID.fromString(standId) }.getOrNull() ?: return
            if (isAdding) {
                safeApiCall { favoritesApiProvider().addFavoriteStand(uuid) }
            } else {
                safeApiCall { favoritesApiProvider().removeFavoriteStand(uuid) }
            }
        }
    }

    override suspend fun syncWithRemote() {
        if (authRepository.currentToken() == null) {
            return
        }
        val local = _favoriteStandIds.value
        val remoteRes = safeApiCall { favoritesApiProvider().listFavoriteStandIds() }
        if (remoteRes is NetworkResult.Success) {
            val remoteSet = remoteRes.data.map { it.toString() }.toSet()
            val merged = local + remoteSet
            saveLocalFavorites(merged)

            // Push any local-only favorites to the backend so nothing is lost on login
            val localOnly = local - remoteSet
            for (standId in localOnly) {
                val uuid = runCatching { UUID.fromString(standId) }.getOrNull() ?: continue
                safeApiCall { favoritesApiProvider().addFavoriteStand(uuid) }
            }
        }
    }

    private fun loadLocalFavorites(): Set<String> =
        prefs.getStringSet(KEY_FAVORITE_STAND_IDS, emptySet())?.toSet() ?: emptySet()

    private fun saveLocalFavorites(ids: Set<String>) {
        prefs.edit().putStringSet(KEY_FAVORITE_STAND_IDS, ids).apply()
        _favoriteStandIds.value = ids
    }

    companion object {
        private const val PREFS_NAME = "sanmartino_favorites_prefs"
        private const val KEY_FAVORITE_STAND_IDS = "favorite_stand_ids"
    }
}
