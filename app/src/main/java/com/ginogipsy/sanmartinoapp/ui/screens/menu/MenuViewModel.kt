package com.ginogipsy.sanmartinoapp.ui.screens.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ginogipsy.sanmartinoapp.data.model.Cantina
import com.ginogipsy.sanmartinoapp.data.model.MenuItem
import com.ginogipsy.sanmartinoapp.data.model.MenuKind
import com.ginogipsy.sanmartinoapp.data.repository.StandRepository
import com.ginogipsy.sanmartinoapp.ui.sanMartinoApplication
import com.ginogipsy.sanmartinoapp.ui.state.UiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MenuData(
    val cantina: Cantina,
    val items: List<MenuItem>,
)

class MenuViewModel(
    private val cantinaId: String,
    val kind: MenuKind,
    private val standRepository: StandRepository,
) : ViewModel() {

    val uiState: StateFlow<UiState<MenuData>> =
        standRepository.getStandStream(cantinaId)
            .map { cantina ->
                if (cantina == null) UiState.Loading
                else {
                    val items = when (kind) {
                        MenuKind.FOOD -> cantina.foods
                        MenuKind.DRINK -> cantina.drinks
                    }
                    UiState.Success(MenuData(cantina = cantina, items = items))
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
            standRepository.getStand(cantinaId)
        }
    }

    companion object {
        fun factory(cantinaId: String, kind: MenuKind): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    MenuViewModel(
                        cantinaId = cantinaId,
                        kind = kind,
                        standRepository = sanMartinoApplication().container.standRepository,
                    )
                }
            }
    }
}
