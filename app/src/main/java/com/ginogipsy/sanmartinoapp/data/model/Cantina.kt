package com.ginogipsy.sanmartinoapp.data.model

data class Cantina(
    val id: String,
    val number: Int,
    val name: String,
    val description: LocalizedText,
    val firstParticipationYear: Int,
    val latitude: Double,
    val longitude: Double,
    val foods: List<MenuItem>,
    val drinks: List<MenuItem>,
)
