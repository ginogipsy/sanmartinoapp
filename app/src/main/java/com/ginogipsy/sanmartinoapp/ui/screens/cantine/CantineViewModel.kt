package com.ginogipsy.sanmartinoapp.ui.screens.cantine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    private val _query = MutableStateFlow("")

    val query: StateFlow<String> = _query.asStateFlow()

    /**
     * Lo stato della lista filtrato per query.
     */
    val results: StateFlow<UiState<List<CantinaSearchResult>>> =
        combine(standRepository.getStandsStream(), _query) { cantine, q ->
            UiState.Success(searchCantine(q, cantine.sortedBy { it.number }))
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
