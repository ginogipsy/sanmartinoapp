package com.ginogipsy.sanmartinoapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ginogipsy.sanmartinoapp.data.model.Cantina
import com.ginogipsy.sanmartinoapp.data.model.LocalizedText

@Entity(tableName = "cantine")
data class CantinaEntity(
    @PrimaryKey val id: String,
    val number: Int,
    val name: String,
    val descriptionIt: String,
    val descriptionEn: String,
    val firstParticipationYear: Int,
    val latitude: Double,
    val longitude: Double
)

fun CantinaEntity.toDomain(): Cantina = Cantina(
    id = id,
    number = number,
    name = name,
    description = LocalizedText(it = descriptionIt, en = descriptionEn),
    firstParticipationYear = firstParticipationYear,
    latitude = latitude,
    longitude = longitude,
    foods = emptyList(),
    drinks = emptyList()
)

fun Cantina.toEntity(): CantinaEntity = CantinaEntity(
    id = id,
    number = number,
    name = name,
    descriptionIt = description.it,
    descriptionEn = description.en,
    firstParticipationYear = firstParticipationYear,
    latitude = latitude,
    longitude = longitude
)
