package com.ginogipsy.sanmartinoapp.ui.screens.cantine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ginogipsy.sanmartinoapp.data.model.StandInteractionType
import com.ginogipsy.sanmartinoapp.data.repository.AuthRepository
import com.ginogipsy.sanmartinoapp.data.repository.FavoritesRepository
import com.ginogipsy.sanmartinoapp.data.repository.StandRepository
import com.ginogipsy.sanmartinoapp.data.search.CantinaSearchResult
import com.ginogipsy.sanmartinoapp.data.search.searchCantine
import com.ginogipsy.sanmartinoapp.network.NetworkResult
import com.ginogipsy.sanmartinoapp.ui.sanMartinoApplication
import com.ginogipsy.sanmartinoapp.ui.state.UiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class CantineViewModel(
    private val standRepository: StandRepository,
    private val favoritesRepository: FavoritesRepository? = null,
    private val authRepository: AuthRepository? = null,
    private val eventId: String? = null,
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _onlyFavorites = MutableStateFlow(false)
    val onlyFavorites: StateFlow<Boolean> = _onlyFavorites.asStateFlow()

    val favoriteStandIds: StateFlow<Set<String>> =
        favoritesRepository?.favoriteStandIds ?: MutableStateFlow(emptySet())

    private val remoteSearchResults = _query
        .debounce(300)
        .distinctUntilChanged()
        .flatMapLatest { q ->
            flow {
                val trimmed = q.trim()
                if (trimmed.isEmpty()) {
                    emit(null)
                } else {
                    when (val res = standRepository.searchStands(query = trimmed, eventId = eventId)) {
                        is NetworkResult.Success -> emit(res.data)
                        else -> emit(null)
                    }
                }
            }
        }

    val results: StateFlow<UiState<List<CantinaSearchResult>>> =
        combine(
            standRepository.getStandsStream(eventId),
            _query,
            remoteSearchResults,
            _onlyFavorites,
            favoriteStandIds,
        ) { cantine, q, remoteHits, filterFavorites, favIds ->
            val sortedCantine = cantine.sortedBy { it.number }
            val baseList = if (q.isBlank()) {
                searchCantine("", sortedCantine)
            } else if (remoteHits != null) {
                val localById = sortedCantine.associateBy { it.id }
                remoteHits.map { hit ->
                    val cached = localById[hit.cantina.id]
                    if (cached != null) hit.copy(cantina = cached) else hit
                }
            } else {
                searchCantine(q, sortedCantine)
            }

            val finalList = if (filterFavorites) {
                baseList.filter { favIds.contains(it.cantina.id) }
            } else {
                baseList
            }
            UiState.Success(finalList)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UiState.Loading,
        )

    init {
        refresh()
        viewModelScope.launch {
            authRepository?.currentUser?.collect { user ->
                if (user != null) {
                    favoritesRepository?.syncWithRemote()
                }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            standRepository.refreshStands(eventId)
            favoritesRepository?.syncWithRemote()
        }
    }

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
    }

    fun clearQuery() {
        _query.value = ""
    }

    fun toggleOnlyFavorites() {
        _onlyFavorites.value = !_onlyFavorites.value
    }

    fun setOnlyFavorites(onlyFavorites: Boolean) {
        _onlyFavorites.value = onlyFavorites
    }

    fun toggleFavorite(cantinaId: String) {
        viewModelScope.launch {
            favoritesRepository?.toggleFavorite(cantinaId)
        }
    }

    fun recordStandInteraction(cantinaId: String, interactionType: StandInteractionType) {
        val currentQuery = _query.value.trim().takeIf { it.isNotEmpty() }
        viewModelScope.launch {
            standRepository.recordInteraction(
                standId = cantinaId,
                interactionType = interactionType,
                searchQuery = currentQuery,
            )
        }
    }

    companion object {
        fun factory(eventId: String? = null): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    val container = sanMartinoApplication().container
                    CantineViewModel(
                        standRepository = container.standRepository,
                        favoritesRepository = container.favoritesRepository,
                        authRepository = container.authRepository,
                        eventId = eventId,
                    )
                }
            }
    }
}
