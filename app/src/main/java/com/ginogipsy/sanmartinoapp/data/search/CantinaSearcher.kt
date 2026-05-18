package com.ginogipsy.sanmartinoapp.data.search

import com.ginogipsy.sanmartinoapp.data.MockRepository
import com.ginogipsy.sanmartinoapp.data.model.Cantina
import com.ginogipsy.sanmartinoapp.data.model.MenuItem

data class CantinaSearchResult(
    val cantina: Cantina,
    val matchingFoods: List<MenuItem>,
    val matchingDrinks: List<MenuItem>,
) {
    val matchCount: Int = matchingFoods.size + matchingDrinks.size
}

interface CantinaSearcher {
    /** Empty / blank query → all cantine, no matching items. */
    fun search(query: String): List<CantinaSearchResult>
}

object LocalCantinaSearcher : CantinaSearcher {

    override fun search(query: String): List<CantinaSearchResult> {
        val normalized = query.trim().lowercase()
        val all = MockRepository.getCantine()
        if (normalized.isEmpty()) {
            return all.map { CantinaSearchResult(it, emptyList(), emptyList()) }
        }
        return all
            .map { cantina ->
                val foods = cantina.foods.filter { it.matches(normalized) }
                val drinks = cantina.drinks.filter { it.matches(normalized) }
                CantinaSearchResult(cantina, foods, drinks)
            }
            .filter { it.matchCount > 0 }
    }

    private fun MenuItem.matches(normalizedQuery: String): Boolean =
        name.lowercase().contains(normalizedQuery) ||
                description.it.lowercase().contains(normalizedQuery) ||
                description.en.lowercase().contains(normalizedQuery)
}
