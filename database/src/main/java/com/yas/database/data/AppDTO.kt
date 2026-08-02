package com.yas.database.data

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "app")
data class AppDTO(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var packageName: String,
    var name: String,
    var isSelected: Boolean,
    var iconPath: String? = null,
)