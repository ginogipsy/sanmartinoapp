package com.ginogipsy.sanmartinoapp.data.search

import com.ginogipsy.sanmartinoapp.data.model.Cantina
import com.ginogipsy.sanmartinoapp.data.model.MenuItem

data class CantinaSearchResult(
    val cantina: Cantina,
    val matchingFoods: List<MenuItem>,
    val matchingDrinks: List<MenuItem>,
) {
    val matchCount: Int = matchingFoods.size + matchingDrinks.size
}

/**
 * Filtro client-side sulle cantine: match su nome / descrizione cantina e — quando il
 * menu e' gia' caricato (dettaglio cantina) — anche sui piatti.
 *
 * Caveat: la lista cantine arriva da `GET /v1/stands` che e' la versione *summary*
 * senza menu, quindi `cantina.foods`/`drinks` saranno vuoti finche' l'utente non apre
 * il dettaglio. Per la ricerca cross-cantine sui piatti servira' un endpoint search
 * server-side (predisposto nel backend via `MenuItem.keywords[]`). Quando arrivera',
 * questa funzione restera' valida come fallback locale.
 */
fun searchCantine(query: String, cantine: List<Cantina>): List<CantinaSearchResult> {
    val normalized = query.trim().lowercase()
    if (normalized.isEmpty()) {
        return cantine.map { CantinaSearchResult(it, emptyList(), emptyList()) }
    }
    return cantine
        .filter { it.matchesAny(normalized) }
        .map { cantina ->
            CantinaSearchResult(
                cantina = cantina,
                matchingFoods = cantina.foods.filter { it.matches(normalized) },
                matchingDrinks = cantina.drinks.filter { it.matches(normalized) },
            )
        }
}

private fun Cantina.matchesAny(normalizedQuery: String): Boolean =
    name.lowercase().contains(normalizedQuery) ||
        description.it.lowercase().contains(normalizedQuery) ||
        description.en.lowercase().contains(normalizedQuery) ||
        foods.any { it.matches(normalizedQuery) } ||
        drinks.any { it.matches(normalizedQuery) }

private fun MenuItem.matches(normalizedQuery: String): Boolean =
    name.lowercase().contains(normalizedQuery) ||
        description.it.lowercase().contains(normalizedQuery) ||
        description.en.lowercase().contains(normalizedQuery) ||
        keywords.any { it.lowercase().contains(normalizedQuery) }
