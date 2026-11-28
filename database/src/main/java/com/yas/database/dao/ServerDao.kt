package com.yas.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.yas.database.data.ServerDTO
import com.yas.database.data.SubscriptionDTO
import kotlinx.coroutines.flow.Flow

@Dao
interface ServerDao {

    @Query("SELECT * FROM server")
    suspend fun getAll(): List<ServerDTO>

    @Query("SELECT * FROM server")
    fun getAllFlow(): Flow<List<ServerDTO>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(data: ServerDTO)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(items: List<ServerDTO>)

    @Delete
    suspend fun remove(data: ServerDTO)

    @Query("DELETE FROM server")
    suspend fun removeAll()

    @Query("UPDATE server SET pingMs = :pingMs WHERE id = :id AND config = :config")
    suspend fun updatePing(
        id: Int,
        config: String,
        pingMs: Long,
    )

    @Query("UPDATE server SET pingMs = -1 WHERE pingMs != -1")
    suspend fun clearPing()

    @Query("SELECT * FROM subscription ORDER BY url")
    fun subscriptions(): Flow<List<SubscriptionDTO>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubscription(subscription: SubscriptionDTO)

    @Query("SELECT * FROM server WHERE subscriptionUrl IS :subscriptionUrl")
    suspend fun getBySource(subscriptionUrl: String?): List<ServerDTO>

    @Transaction
    suspend fun saveImport(
        items: List<ServerDTO>,
        subscription: SubscriptionDTO?,
    ) {
        require(items.isNotEmpty())
        val subscriptionUrl = subscription?.url
        if (subscription != null) {
            insertSubscription(
                subscription = subscription.copy(
                    title = subscription.title ?: getSubscriptionTitle(url = subscription.url),
                ),
            )
        }
        val existing = getBySource(subscriptionUrl = subscriptionUrl)
        val byConfig = existing.associateBy { it.config.importKey() }
        val retained = mutableSetOf<Int>()
        for (item in items) {
            val previous = byConfig[item.config.importKey()]
            if (previous != null) retained.add(previous.id)
            if (previous == null || subscriptionUrl != null) {
                insert(
                    data = item.copy(
                        id = previous?.id ?: 0,
                        pingMs = previous?.pingMs ?: -1L,
                        subscriptionUrl = subscriptionUrl,
                    ),
                )
            }
        }
        if (subscriptionUrl != null) {
            existing.filterNot { it.id in retained }.forEach { remove(data = it) }
        }
    }

    @Query("SELECT title FROM subscription WHERE url = :url")
    suspend fun getSubscriptionTitle(url: String): String?

    @Query("DELETE FROM server WHERE subscriptionUrl = :url")
    suspend fun deleteSubscriptionServers(url: String)

    @Query("DELETE FROM subscription WHERE url = :url")
    suspend fun deleteSubscriptionRecord(url: String)

    @Transaction
    suspend fun deleteSubscription(url: String) {
        deleteSubscriptionServers(url = url)
        deleteSubscriptionRecord(url = url)
    }


}

private fun String.importKey(): String =
    if (trimStart().startsWith('{')) this else substringBefore('#')
