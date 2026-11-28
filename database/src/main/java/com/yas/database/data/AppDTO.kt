package com.yas.database.data

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity(
    tableName = "app",
    indices = [
        Index(
            value = ["packageName"],
            unique = true,
        ),
    ],
)
data class AppDTO(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val packageName: String,
    val name: String,
    val isSelected: Boolean,
    val iconPath: String? = null,
) {
    @get:Ignore
    val iconPathNotNull: String get() = iconPath.orEmpty()
}
