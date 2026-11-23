package com.ginogipsy.sanmartinoapp.ui

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ginogipsy.sanmartinoapp.SanMartinoApplication
import com.ginogipsy.sanmartinoapp.ui.screens.cantine.CantineViewModel
import com.ginogipsy.sanmartinoapp.ui.screens.events.EventsViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            EventsViewModel(sanMartinoApplication().container.eventRepository)
        }
        initializer {
            val container = sanMartinoApplication().container
            CantineViewModel(
                standRepository = container.standRepository,
                favoritesRepository = container.favoritesRepository,
                authRepository = container.authRepository,
            )
        }
    }
}

fun CreationExtras.sanMartinoApplication(): SanMartinoApplication =
    this[AndroidViewModelFactory.APPLICATION_KEY] as SanMartinoApplication
