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
import com.ginogipsy.sanmartinoapp.network.NetworkResult
import com.ginogipsy.sanmartinoapp.ui.sanMartinoApplication
import com.ginogipsy.sanmartinoapp.ui.state.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    private val _uiState = MutableStateFlow<UiState<MenuData>>(UiState.Loading)
    val uiState: StateFlow<UiState<MenuData>> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            _uiState.value = when (val r = standRepository.getStand(cantinaId)) {
                is NetworkResult.Success -> {
                    val items = when (kind) {
                        MenuKind.FOOD -> r.data.foods
                        MenuKind.DRINK -> r.data.drinks
                    }
                    UiState.Success(MenuData(cantina = r.data, items = items))
                }
                is NetworkResult.Error -> UiState.Error(r.message)
            }
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
