package com.yas.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.yas.database.data.ServerDTO
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.Flow

@Dao
interface ServerDao {

    @Query("SELECT * FROM server")
    suspend fun getAll(): List<ServerDTO>

    @Query("SELECT * FROM server")
    fun getAllFlow(): Flow<List<ServerDTO>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(data: ServerDTO)

    suspend fun insert(items: List<ServerDTO>) {
        if (items.isEmpty()) {
            return
        }

        items.forEach {
            insert(it)
        }
    }

    @Delete
    suspend fun remove(data: ServerDTO)

    @Query("DELETE FROM server")
    suspend fun removeAll()


}