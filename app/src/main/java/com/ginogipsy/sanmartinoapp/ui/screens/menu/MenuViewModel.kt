package com.ginogipsy.sanmartinoapp.ui.screens.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ginogipsy.sanmartinoapp.data.MockRepository
import com.ginogipsy.sanmartinoapp.data.model.Cantina
import com.ginogipsy.sanmartinoapp.data.model.MenuItem
import com.ginogipsy.sanmartinoapp.data.model.MenuKind

class MenuViewModel(
    cantinaId: String,
    val kind: MenuKind,
) : ViewModel() {
    val cantina: Cantina? = MockRepository.getCantina(cantinaId)
    val items: List<MenuItem> = when (kind) {
        MenuKind.FOOD -> cantina?.foods.orEmpty()
        MenuKind.DRINK -> cantina?.drinks.orEmpty()
    }

    companion object {
        fun factory(cantinaId: String, kind: MenuKind): ViewModelProvider.Factory =
            viewModelFactory {
                initializer { MenuViewModel(cantinaId, kind) }
            }
    }
}
