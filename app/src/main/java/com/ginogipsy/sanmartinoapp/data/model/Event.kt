package com.ginogipsy.sanmartinoapp.data.model

import java.time.LocalDate

data class Event(
    val id: String,
    val name: String,
    val place: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val description: LocalizedText,
) {
    fun isUpcoming(today: LocalDate = LocalDate.now()): Boolean = !endDate.isBefore(today)
}
