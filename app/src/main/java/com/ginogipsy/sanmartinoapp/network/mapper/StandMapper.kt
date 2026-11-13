package com.ginogipsy.sanmartinoapp.network.mapper

import com.ginogipsy.sanmartinoapp.data.model.Cantina
import com.ginogipsy.sanmartinoapp.data.model.LocalizedText
import com.ginogipsy.sanmartinoapp.data.model.MenuItem
import com.ginogipsy.sanmartinoapp.data.model.MenuKind
import com.ginogipsy.sanmartinoapp.data.search.CantinaSearchResult
import com.ginogipsy.sanmartinoapp.network.generated.stands.model.LocalizedTextDto
import com.ginogipsy.sanmartinoapp.network.generated.stands.model.MenuItemDto
import com.ginogipsy.sanmartinoapp.network.generated.stands.model.MenuKindDto
import com.ginogipsy.sanmartinoapp.network.generated.stands.model.StandDetailDto
import com.ginogipsy.sanmartinoapp.network.generated.stands.model.StandSearchResultDto
import com.ginogipsy.sanmartinoapp.network.generated.stands.model.StandSummaryDto

/**
 * Mapper dalle DTO generate da OpenAPI per stands-api verso il modello UI.
 *
 * `StandSummary` non porta menu / owners (sono solo nel `StandDetail`): nella lista
 * popoliamo `foods`/`drinks` come liste vuote, il dettaglio le riempira'.
 *
 * Il backend espone un unico array `menuItems[]` con campo `kind` (FOOD | DRINK),
 * mentre il modello UI tiene `foods` e `drinks` separati: qui partizioniamo.
 */
fun StandSummaryDto.toDomain(): Cantina = Cantina(
    id = id.toString(),
    number = number,
    name = name,
    description = description.toDomain(),
    firstParticipationYear = firstParticipationYear,
    latitude = latitude,
    longitude = longitude,
    foods = emptyList(),
    drinks = emptyList(),
)

fun StandDetailDto.toDomain(): Cantina {
    val (foods, drinks) = menuItems.partition { it.kind == MenuKindDto.FOOD }
    return Cantina(
        id = id.toString(),
        number = number,
        name = name,
        description = description.toDomain(),
        firstParticipationYear = firstParticipationYear,
        latitude = latitude,
        longitude = longitude,
        foods = foods.map { it.toDomain() },
        drinks = drinks.map { it.toDomain() },
    )
}

fun StandSearchResultDto.toDomain(cachedCantina: Cantina? = null): CantinaSearchResult {
    val (matchedFoodsDto, matchedDrinksDto) = matchedItems.partition { it.kind == MenuKindDto.FOOD }
    val matchingFoods = matchedFoodsDto.map { it.toDomain() }
    val matchingDrinks = matchedDrinksDto.map { it.toDomain() }
    val baseCantina = stand.toDomain().copy(
        foods = cachedCantina?.foods?.takeIf { it.isNotEmpty() } ?: matchingFoods,
        drinks = cachedCantina?.drinks?.takeIf { it.isNotEmpty() } ?: matchingDrinks,
    )
    return CantinaSearchResult(
        cantina = baseCantina,
        matchingFoods = matchingFoods,
        matchingDrinks = matchingDrinks,
        score = score,
    )
}

internal fun MenuItemDto.toDomain(): MenuItem = MenuItem(
    id = id.toString(),
    name = name,
    description = description.toDomain(),
    availablePlates = availablePlates,
    kind = kind.toDomain(),
    keywords = keywords ?: emptyList(),
)

internal fun MenuKindDto.toDomain(): MenuKind = when (this) {
    MenuKindDto.FOOD -> MenuKind.FOOD
    MenuKindDto.DRINK -> MenuKind.DRINK
}

internal fun MenuKind.toDto(): MenuKindDto = when (this) {
    MenuKind.FOOD -> MenuKindDto.FOOD
    MenuKind.DRINK -> MenuKindDto.DRINK
}

internal fun LocalizedTextDto.toDomain(): LocalizedText =
    LocalizedText(it = `it`, en = en)
