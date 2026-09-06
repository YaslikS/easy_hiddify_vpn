package com.yas.database.data

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "server")
data class ServerDTO(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var config: String,
    var name: String,
)