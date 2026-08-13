package com.ginogipsy.sanmartinoapp.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation
import com.ginogipsy.sanmartinoapp.data.model.Cantina
import com.ginogipsy.sanmartinoapp.data.model.LocalizedText

data class CantinaWithMenuItems(
    @Embedded val cantina: CantinaEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "standId"
    )
    val menuItems: List<MenuItemEntity>
)

fun CantinaWithMenuItems.toDomain(): Cantina {
    val foods = menuItems.filter { it.kind == "FOOD" }.map { it.toDomain() }
    val drinks = menuItems.filter { it.kind == "DRINK" }.map { it.toDomain() }
    return cantina.toDomain().copy(foods = foods, drinks = drinks)
}
