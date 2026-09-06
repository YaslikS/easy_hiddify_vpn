package com.yas.database.repo

import android.content.Context
import com.yas.database.AppDB
import com.yas.database.dao.ServerDao
import com.yas.database.data.ServerDTO
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


class ServerRepo private constructor(
    private val dao: ServerDao,
) : ServerDao {

    companion object {
        @Synchronized
        fun provide(context: Context): ServerRepo {
            return (ServerRepo(AppDB.getDatabase(context).serverDao()))
        }
    }

    override fun getAllFlow(): Flow<ImmutableList<ServerDTO>> {
        return dao.getAllFlow().map {
            it.toImmutableList()
        }
    }

    override suspend fun getAll(): List<ServerDTO> {
       return dao.getAll()
    }

    override suspend fun insert(data: ServerDTO) {
        dao.insert(data)
    }

    override suspend fun insert(items: List<ServerDTO>) {
        dao.insert(items)
    }

    override suspend fun removeAll() {
        dao.removeAll()
    }

    override suspend fun remove(data: ServerDTO) {
        dao.remove(data)
    }

}