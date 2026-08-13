package com.ginogipsy.sanmartinoapp.data.model

enum class MenuKind { FOOD, DRINK }

data class MenuItem(
    val id: String,
    val name: String,
    val description: LocalizedText,
    val availablePlates: Int,
    val kind: MenuKind,
    val keywords: List<String> = emptyList(),
)
