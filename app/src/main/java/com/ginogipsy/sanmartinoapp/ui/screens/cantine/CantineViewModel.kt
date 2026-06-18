package com.ginogipsy.sanmartinoapp.ui.screens.cantine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ginogipsy.sanmartinoapp.data.model.Cantina
import com.ginogipsy.sanmartinoapp.data.repository.StandRepository
import com.ginogipsy.sanmartinoapp.data.search.CantinaSearchResult
import com.ginogipsy.sanmartinoapp.data.search.searchCantine
import com.ginogipsy.sanmartinoapp.network.NetworkResult
import com.ginogipsy.sanmartinoapp.ui.state.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CantineViewModel(
    private val standRepository: StandRepository,
) : ViewModel() {

    private val _cantineState = MutableStateFlow<UiState<List<Cantina>>>(UiState.Loading)
    private val _query = MutableStateFlow("")

    val query: StateFlow<String> = _query.asStateFlow()

    /**
     * Lo stato della lista filtrato per query. `combine` rieemette ogni volta che cambia
     * la lista (refresh dal backend) o la query (digitazione).
     */
    val results: StateFlow<UiState<List<CantinaSearchResult>>> =
        combine(_cantineState, _query) { state, q ->
            when (state) {
                is UiState.Loading -> UiState.Loading
                is UiState.Error -> state
                is UiState.Success -> UiState.Success(searchCantine(q, state.data))
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
            _cantineState.value = UiState.Loading
            _cantineState.value = when (val r = standRepository.getStands()) {
                is NetworkResult.Success -> UiState.Success(r.data.sortedBy { it.number })
                is NetworkResult.Error -> UiState.Error(r.message)
            }
        }
    }

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
    }

    fun clearQuery() {
        _query.value = ""
    }
}
