package com.ginogipsy.sanmartinoapp.data.repository

import com.ginogipsy.sanmartinoapp.data.model.Cantina
import com.ginogipsy.sanmartinoapp.data.model.FestivalReview
import com.ginogipsy.sanmartinoapp.data.model.MenuKind
import com.ginogipsy.sanmartinoapp.data.model.ReviewStatus
import com.ginogipsy.sanmartinoapp.data.model.ReviewTargetType
import com.ginogipsy.sanmartinoapp.data.model.StandInteractionType
import com.ginogipsy.sanmartinoapp.data.search.CantinaSearchResult
import com.ginogipsy.sanmartinoapp.network.NetworkResult
import kotlinx.coroutines.flow.Flow

interface StandRepository {
    /** Stream delle cantine (con menu locale se disponibile). */
    fun getStandsStream(): Flow<List<Cantina>>

    /** Forza il refresh delle cantine dal backend. */
    suspend fun refreshStands(): NetworkResult<Unit>

    /** Dettaglio singola cantina, comprensivo di menu (food + drinks). */
    suspend fun getStand(id: String): NetworkResult<Cantina>

    /** Stream del dettaglio di una cantina. */
    fun getStandStream(id: String): Flow<Cantina?>

    /**
     * Ricerca intelligente server-side su `/v1/stands/search` per nome piatto,
     * parole chiave (`keywords`) e descrizione.
     */
    suspend fun searchStands(
        query: String,
        kind: MenuKind? = null,
        onlyAvailable: Boolean = false,
    ): NetworkResult<List<CantinaSearchResult>>

    /**
     * Registra un click su un piatto o un'interazione con una cantina
     * su `/v1/stands/analytics/interactions`.
     */
    suspend fun recordInteraction(
        standId: String,
        interactionType: StandInteractionType,
        menuItemId: String? = null,
        searchQuery: String? = null,
    ): NetworkResult<Unit>

    /**
     * Carica le recensioni (di default solo quelle approvate dai moderatori) per un target.
     */
    suspend fun listReviews(
        targetType: ReviewTargetType,
        targetId: String,
        status: ReviewStatus? = ReviewStatus.APPROVED,
    ): NetworkResult<List<FestivalReview>>

    /**
     * Invia una nuova recensione (messa in coda di moderazione PENDING).
     */
    suspend fun createReview(
        targetType: ReviewTargetType,
        targetId: String,
        standId: String? = null,
        targetName: String? = null,
        authorUsername: String? = null,
        rating: Int,
        comment: String,
    ): NetworkResult<FestivalReview>
}
