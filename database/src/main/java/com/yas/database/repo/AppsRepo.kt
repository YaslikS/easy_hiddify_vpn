package com.yas.database.repo

import android.content.Context
import com.yas.database.AppDB
import com.yas.database.dao.AppsDao
import com.yas.database.data.AppDTO
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


class AppsRepo private constructor(
    private val dao: AppsDao,
) : AppsDao {

    companion object {
        @Synchronized
        fun provide(context: Context): AppsRepo {
            return (AppsRepo(AppDB.getDatabase(context).appsDao()))
        }
    }

    override fun getAllFlow(): Flow<ImmutableList<AppDTO>> {
        return dao.getAllFlow().map {
            it.toImmutableList()
        }
    }

    override suspend fun getAll(): List<AppDTO> {
       return dao.getAll()
    }

    override suspend fun insert(data: AppDTO) {
        dao.insert(data)
    }

    override suspend fun insert(items: List<AppDTO>) {
        dao.insert(items)
    }

    override suspend fun removeByPackageName(packageName: String) {
        dao.removeByPackageName(packageName)
    }

    override suspend fun removeAll() {
        dao.removeAll()
    }

    override suspend fun remove(data: AppDTO) {
        dao.remove(data)
    }

}