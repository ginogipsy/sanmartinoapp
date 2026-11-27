package com.ginogipsy.sanmartinoapp.data.search

import com.ginogipsy.sanmartinoapp.data.model.Cantina
import com.ginogipsy.sanmartinoapp.data.model.MenuItem
import java.text.Normalizer
import java.util.Locale

data class CantinaSearchResult(
    val cantina: Cantina,
    val matchingFoods: List<MenuItem>,
    val matchingDrinks: List<MenuItem>,
    val score: Int = 0,
) {
    val matchCount: Int = matchingFoods.size + matchingDrinks.size
}

private val STOP_WORDS = setOf(
    "il", "lo", "la", "i", "gli", "le", "un", "uno", "una",
    "di", "a", "da", "in", "con", "su", "per", "tra", "fra",
    "al", "allo", "alla", "ai", "agli", "alle",
    "del", "dello", "della", "dei", "degli", "delle",
    "nel", "nella", "sul", "sulla", "col", "e", "o", "the", "with", "of", "and"
)

/**
 * Motore di ricerca e ranking client-side (usato anche come fallback offline istantaneo)
 * allineato all'algoritmo server-side di `/v1/stands/search`:
 * - tokenizza la query rimuovendo accenti e stopword
 * - calcola uno score su nome piatto, `keywords` (es. pasta, primo, sugo, guanciale) e descrizioni
 * - ordina le cantine per punteggio decrescente.
 */
fun searchCantine(query: String, cantine: List<Cantina>): List<CantinaSearchResult> {
    val fullNorm = normalizeText(query)
    val tokens = tokenizeQuery(query)
    if (tokens.isEmpty()) {
        return cantine.map { CantinaSearchResult(it, emptyList(), emptyList(), 0) }
    }

    val results = mutableListOf<CantinaSearchResult>()

    for (cantina in cantine) {
        val standScore = scoreStandMetadata(cantina, fullNorm, tokens)

        val scoredFoods = cantina.foods
            .map { it to scoreMenuItem(it, fullNorm, tokens) }
            .filter { it.second > 0 }
            .sortedByDescending { it.second }

        val scoredDrinks = cantina.drinks
            .map { it to scoreMenuItem(it, fullNorm, tokens) }
            .filter { it.second > 0 }
            .sortedByDescending { it.second }

        val itemsScore = scoredFoods.sumOf { it.second } + scoredDrinks.sumOf { it.second }
        val totalScore = standScore + itemsScore

        if (totalScore > 0) {
            results.add(
                CantinaSearchResult(
                    cantina = cantina,
                    matchingFoods = scoredFoods.map { it.first },
                    matchingDrinks = scoredDrinks.map { it.first },
                    score = totalScore,
                )
            )
        }
    }

    return results.sortedWith(
        compareByDescending<CantinaSearchResult> { it.score }
            .thenBy { it.cantina.number }
    )
}

private fun scoreMenuItem(item: MenuItem, fullQuery: String, tokens: List<String>): Int {
    val nameNorm = normalizeText(item.name)
    val descItNorm = normalizeText(item.description.it)
    val descEnNorm = normalizeText(item.description.en)
    val keywordsNorm = item.keywords.map(::normalizeText).filter { it.isNotEmpty() }

    var score = 0
    if (fullQuery.isNotEmpty()) {
        if (nameNorm == fullQuery) score += 150
        else if (nameNorm.contains(fullQuery)) score += 90
        if (keywordsNorm.contains(fullQuery)) score += 130
    }

    var matchedTokens = 0
    for (token in tokens) {
        var tokenScore = 0
        if (nameNorm == token) tokenScore = maxOf(tokenScore, 100)
        else if (containsWordPrefix(nameNorm, token)) tokenScore = maxOf(tokenScore, 75)
        else if (nameNorm.contains(token)) tokenScore = maxOf(tokenScore, 55)

        for (kw in keywordsNorm) {
            if (kw == token) tokenScore = maxOf(tokenScore, 100)
            else if (kw.startsWith(token) || token.startsWith(kw)) tokenScore = maxOf(tokenScore, 70)
            else if (kw.contains(token)) tokenScore = maxOf(tokenScore, 50)
        }

        if (containsWordPrefix(descItNorm, token) || containsWordPrefix(descEnNorm, token)) {
            tokenScore = maxOf(tokenScore, 35)
        } else if (descItNorm.contains(token) || descEnNorm.contains(token)) {
            tokenScore = maxOf(tokenScore, 20)
        }

        if (tokenScore > 0) {
            matchedTokens++
            score += tokenScore
        }
    }

    if (score == 0) return 0
    if (tokens.size > 1 && matchedTokens == tokens.size) score += 40
    if (item.availablePlates > 0) score += 15
    return score
}

private fun scoreStandMetadata(cantina: Cantina, fullQuery: String, tokens: List<String>): Int {
    val nameNorm = normalizeText(cantina.name)
    val descItNorm = normalizeText(cantina.description.it)
    val descEnNorm = normalizeText(cantina.description.en)
    val numStr = cantina.number.toString()

    var score = 0
    if (numStr == fullQuery) score += 120
    else if (fullQuery.isNotEmpty() && nameNorm == fullQuery) score += 110

    for (token in tokens) {
        when {
            numStr == token -> score += 80
            containsWordPrefix(nameNorm, token) -> score += 50
            nameNorm.contains(token) -> score += 35
            descItNorm.contains(token) || descEnNorm.contains(token) -> score += 15
        }
    }
    return score
}

private fun containsWordPrefix(text: String, token: String): Boolean {
    if (text.isEmpty() || token.isEmpty()) return false
    return text.split(" ").any { it.startsWith(token) }
}

private fun tokenizeQuery(query: String): List<String> {
    val normalized = normalizeText(query)
    if (normalized.isEmpty()) return emptyList()
    val raw = normalized.split(" ").filter { it.isNotEmpty() }
    val filtered = raw.filter { !STOP_WORDS.contains(it) || raw.size == 1 }.distinct()
    return filtered.ifEmpty { listOf(normalized) }
}

private fun normalizeText(input: String): String {
    if (input.isBlank()) return ""
    val decomposed = Normalizer.normalize(input.lowercase(Locale.ITALIAN), Normalizer.Form.NFD)
        .replace(Regex("\\p{M}"), "")
    return decomposed.replace(Regex("[^a-z0-9\\s]"), " ").replace(Regex("\\s+"), " ").trim()
}
