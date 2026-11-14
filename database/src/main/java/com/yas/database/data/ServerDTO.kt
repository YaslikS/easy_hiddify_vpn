package com.yas.database.data

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "server")
data class ServerDTO(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val config: String,
    val pingMs: Long = -1L,
)