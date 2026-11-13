package com.ginogipsy.sanmartinoapp.ui.screens.cantine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ginogipsy.sanmartinoapp.data.repository.StandRepository
import com.ginogipsy.sanmartinoapp.data.search.CantinaSearchResult
import com.ginogipsy.sanmartinoapp.data.search.searchCantine
import com.ginogipsy.sanmartinoapp.network.NetworkResult
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
) : ViewModel() {

    private val _query = MutableStateFlow("")

    val query: StateFlow<String> = _query.asStateFlow()

    /**
     * Risultati della ricerca server-side (`/v1/stands/search?q=...`) con debounce,
     * oppure null se la query è vuota o se il dispositivo è offline (fallback su Room).
     */
    private val remoteSearchResults = _query
        .debounce(220)
        .distinctUntilChanged()
        .flatMapLatest { q ->
            flow {
                val trimmed = q.trim()
                if (trimmed.isEmpty()) {
                    emit(null)
                } else {
                    when (val res = standRepository.searchStands(trimmed)) {
                        is NetworkResult.Success -> emit(res.data)
                        else -> emit(null)
                    }
                }
            }
        }

    /**
     * Lo stato della lista filtrato e ordinato per rilevanza (`score`).
     * Usa i risultati dell'algoritmo server-side `/v1/stands/search` quando disponibili,
     * arricchiti con il menu locale e con fallback trasparente offline su `searchCantine`.
     */
    val results: StateFlow<UiState<List<CantinaSearchResult>>> =
        combine(
            standRepository.getStandsStream(),
            _query,
            remoteSearchResults,
        ) { cantine, q, remoteHits ->
            val sortedCantine = cantine.sortedBy { it.number }
            if (q.isBlank()) {
                UiState.Success(searchCantine("", sortedCantine))
            } else if (remoteHits != null) {
                val localById = sortedCantine.associateBy { it.id }
                val merged = remoteHits.map { hit ->
                    val cached = localById[hit.cantina.id]
                    if (cached != null) {
                        hit.copy(
                            cantina = cached,
                        )
                    } else {
                        hit
                    }
                }
                UiState.Success(merged)
            } else {
                UiState.Success(searchCantine(q, sortedCantine))
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UiState.Loading,
        )

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            standRepository.refreshStands()
        }
    }

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
    }

    fun clearQuery() {
        _query.value = ""
    }
}
