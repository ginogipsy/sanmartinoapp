package com.ginogipsy.sanmartinoapp.ui.screens.cantine

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.ginogipsy.sanmartinoapp.data.search.CantinaSearchResult
import com.ginogipsy.sanmartinoapp.data.search.CantinaSearcher
import com.ginogipsy.sanmartinoapp.data.search.LocalCantinaSearcher

class CantineViewModel(
    private val searcher: CantinaSearcher = LocalCantinaSearcher,
) : ViewModel() {

    var query by mutableStateOf("")
        private set

    private val resultsState = derivedStateOf { searcher.search(query) }
    val results: List<CantinaSearchResult> get() = resultsState.value

    val hasActiveQuery: Boolean get() = query.isNotBlank()

    fun onQueryChange(newQuery: String) {
        query = newQuery
    }

    fun clearQuery() {
        query = ""
    }
}
