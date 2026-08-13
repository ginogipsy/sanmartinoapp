package com.ginogipsy.sanmartinoapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import com.ginogipsy.sanmartinoapp.data.model.LocalizedText
import com.ginogipsy.sanmartinoapp.data.model.MenuItem
import com.ginogipsy.sanmartinoapp.data.model.MenuKind

@Entity(
    tableName = "menu_items",
    foreignKeys = [
        ForeignKey(
            entity = CantinaEntity::class,
            parentColumns = ["id"],
            childColumns = ["standId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["standId"])]
)
data class MenuItemEntity(
    @PrimaryKey val id: String,
    val standId: String,
    val name: String,
    val descriptionIt: String,
    val descriptionEn: String,
    val availablePlates: Int,
    val kind: String, // "FOOD" or "DRINK"
    val keywords: String // Comma separated
)

fun MenuItemEntity.toDomain(): MenuItem = MenuItem(
    id = id,
    name = name,
    description = LocalizedText(it = descriptionIt, en = descriptionEn),
    availablePlates = availablePlates,
    kind = MenuKind.valueOf(kind),
    keywords = if (keywords.isBlank()) emptyList() else keywords.split(",")
)

fun MenuItem.toEntity(standId: String): MenuItemEntity = MenuItemEntity(
    id = id,
    standId = standId,
    name = name,
    descriptionIt = description.it,
    descriptionEn = description.en,
    availablePlates = availablePlates,
    kind = kind.name,
    keywords = keywords.joinToString(",")
)
