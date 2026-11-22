package com.yas.database.data

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "subscription")
data class SubscriptionDTO(
    @PrimaryKey val url: String,
    val expiresAt: Long? = null,
    val title: String? = null,
) {
    @get:Ignore
    val titleNotNull: String get() = title.orEmpty()

    @get:Ignore
    val expiresAtNotNull: Long get() = expiresAt ?: 0L
}
