package com.ginogipsy.sanmartinoapp.ui.screens.events

import androidx.lifecycle.ViewModel
import com.ginogipsy.sanmartinoapp.data.MockRepository
import com.ginogipsy.sanmartinoapp.data.model.Event
import java.time.LocalDate

data class EventsUiState(
    val upcoming: List<Event> = emptyList(),
    val past: List<Event> = emptyList(),
)

class EventsViewModel : ViewModel() {
    val uiState: EventsUiState

    init {
        val today = LocalDate.now()
        val all = MockRepository.getEvents()
        uiState = EventsUiState(
            upcoming = all.filter { it.isUpcoming(today) }.sortedBy { it.startDate },
            past = all.filter { !it.isUpcoming(today) }.sortedByDescending { it.startDate },
        )
    }
}
