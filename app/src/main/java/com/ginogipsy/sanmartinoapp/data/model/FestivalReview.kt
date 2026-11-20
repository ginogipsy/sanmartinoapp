package com.ginogipsy.sanmartinoapp.data.model

import java.time.OffsetDateTime

enum class ReviewTargetType {
    STAND,
    MENU_ITEM,
    EVENT,
}

enum class ReviewStatus {
    PENDING,
    APPROVED,
    REJECTED,
}

data class FestivalReview(
    val id: String,
    val targetType: ReviewTargetType,
    val targetId: String,
    val standId: String? = null,
    val targetName: String,
    val authorUserId: String,
    val authorUsername: String,
    val rating: Int,
    val comment: String,
    val status: ReviewStatus,
    val createdAt: OffsetDateTime,
)
