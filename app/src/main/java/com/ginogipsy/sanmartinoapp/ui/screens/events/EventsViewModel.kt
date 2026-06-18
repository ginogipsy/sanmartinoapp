package com.ginogipsy.sanmartinoapp.ui.screens.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ginogipsy.sanmartinoapp.data.model.Event
import com.ginogipsy.sanmartinoapp.data.repository.EventRepository
import com.ginogipsy.sanmartinoapp.network.NetworkResult
import com.ginogipsy.sanmartinoapp.ui.state.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

data class EventsData(
    val upcoming: List<Event>,
    val past: List<Event>,
)

class EventsViewModel(
    private val eventRepository: EventRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<EventsData>>(UiState.Loading)
    val uiState: StateFlow<UiState<EventsData>> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            _uiState.value = when (val result = eventRepository.getEvents()) {
                is NetworkResult.Success -> UiState.Success(result.data.partitionByStatus())
                is NetworkResult.Error -> UiState.Error(result.message)
            }
        }
    }

    private fun List<Event>.partitionByStatus(today: LocalDate = LocalDate.now()): EventsData {
        val (upcoming, past) = partition { it.isUpcoming(today) }
        return EventsData(
            upcoming = upcoming.sortedBy { it.startDate },
            past = past.sortedByDescending { it.startDate },
        )
    }
}
