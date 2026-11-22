package com.yas.database.repo

import android.content.Context
import com.yas.database.AppDB
import com.yas.database.dao.ServerDao
import com.yas.database.data.ServerDTO
import com.yas.database.data.SubscriptionDTO
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


class ServerRepo private constructor(private val dao: ServerDao) : ServerDao {

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

    override suspend fun updatePing(
        id: Int,
        config: String,
        pingMs: Long,
    ) = dao.updatePing(
        id = id,
        config = config,
        pingMs = pingMs,
    )

    override suspend fun clearPing() = dao.clearPing()

    override suspend fun saveImport(
        items: List<ServerDTO>,
        subscription: SubscriptionDTO?,
    ) = dao.saveImport(
        items = items,
        subscription = subscription,
    )

    override suspend fun getSubscriptionTitle(url: String): String? = dao.getSubscriptionTitle(url = url)

    override fun subscriptions(): Flow<ImmutableList<SubscriptionDTO>> =
        dao.subscriptions().map { it.toImmutableList() }

    override suspend fun deleteSubscriptionServers(url: String) = dao.deleteSubscriptionServers(url = url)

    override suspend fun deleteSubscriptionRecord(url: String) = dao.deleteSubscriptionRecord(url = url)

    override suspend fun deleteSubscription(url: String) = dao.deleteSubscription(url = url)

    override suspend fun insertSubscription(subscription: SubscriptionDTO) =
        dao.insertSubscription(subscription = subscription)

    override suspend fun getBySource(subscriptionUrl: String?): List<ServerDTO> =
        dao.getBySource(subscriptionUrl = subscriptionUrl)
}
