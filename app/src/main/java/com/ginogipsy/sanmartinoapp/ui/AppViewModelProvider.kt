package com.ginogipsy.sanmartinoapp.ui

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ginogipsy.sanmartinoapp.SanMartinoApplication
import com.ginogipsy.sanmartinoapp.ui.screens.cantine.CantineViewModel
import com.ginogipsy.sanmartinoapp.ui.screens.events.EventsViewModel

/**
 * Factory unica per i ViewModel "senza argomenti di routing". `MenuViewModel` ha
 * cantinaId + kind dal back stack quindi resta con la sua factory dedicata.
 *
 * Pattern preso da Now-in-Android / codelab Compose: il container e' agganciato a
 * `SanMartinoApplication`, le initializer leggono da [CreationExtras] per ottenere
 * l'istanza Application senza dover passare il context giu' attraverso le screen.
 */
object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            EventsViewModel(sanMartinoApplication().container.eventRepository)
        }
        initializer {
            CantineViewModel(sanMartinoApplication().container.standRepository)
        }
    }
}

fun CreationExtras.sanMartinoApplication(): SanMartinoApplication =
    this[AndroidViewModelFactory.APPLICATION_KEY] as SanMartinoApplication
