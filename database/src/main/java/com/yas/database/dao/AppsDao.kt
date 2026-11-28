package com.yas.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.yas.database.data.AppDTO
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface AppsDao {

    @Query("SELECT * FROM app")
    suspend fun getAll(): List<AppDTO>

    @Query("SELECT * FROM app")
    fun getAllFlow(): Flow<List<AppDTO>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(data: AppDTO)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(items: List<AppDTO>)

    @Delete
    suspend fun remove(items: List<AppDTO>)

    @Transaction
    suspend fun syncInstalledApps(
        removed: List<AppDTO>,
        changed: List<AppDTO>,
    ) {
        if (removed.isNotEmpty()) remove(items = removed)
        if (changed.isNotEmpty()) insert(items = changed)
    }

    @Delete
    suspend fun remove(data: AppDTO)

    @Query("DELETE FROM app")
    suspend fun removeAll()

    @Query("DELETE FROM app WHERE packageName = :packageName")
    suspend fun removeByPackageName(packageName: String)

}
