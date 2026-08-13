package com.ginogipsy.sanmartinoapp.network.mapper

import com.ginogipsy.sanmartinoapp.data.model.Cantina
import com.ginogipsy.sanmartinoapp.data.model.LocalizedText
import com.ginogipsy.sanmartinoapp.data.model.MenuItem
import com.ginogipsy.sanmartinoapp.data.model.MenuKind
import com.ginogipsy.sanmartinoapp.network.generated.stands.model.LocalizedTextDto
import com.ginogipsy.sanmartinoapp.network.generated.stands.model.MenuItemDto
import com.ginogipsy.sanmartinoapp.network.generated.stands.model.MenuKindDto
import com.ginogipsy.sanmartinoapp.network.generated.stands.model.StandDetailDto
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

internal fun LocalizedTextDto.toDomain(): LocalizedText =
    LocalizedText(it = `it`, en = en)
