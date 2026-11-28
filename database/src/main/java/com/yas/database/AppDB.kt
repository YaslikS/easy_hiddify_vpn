package com.yas.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.yas.database.dao.AppsDao
import com.yas.database.dao.ServerDao
import com.yas.database.data.AppDTO
import com.yas.database.data.ServerDTO
import com.yas.database.data.SubscriptionDTO
import com.yas.database.migrations.MIGRATION_1_2
import com.yas.database.migrations.MIGRATION_2_3
import com.yas.database.migrations.MIGRATION_3_4
import com.yas.database.migrations.MIGRATION_4_5
import com.yas.database.migrations.MIGRATION_5_6
import com.yas.database.migrations.MIGRATION_6_7
import com.yas.database.migrations.MIGRATION_7_8


@Database(
    entities = [
        AppDTO::class,
        ServerDTO::class,
        SubscriptionDTO::class,
    ],
    version = 8,
    exportSchema = true,
)
internal abstract class AppDB : RoomDatabase() {

    companion object {

        private val lock = Any()

        @Volatile
        private var INSTANCE: AppDB? = null

        @Synchronized
        fun getDatabase(context: Context): AppDB {
            return INSTANCE ?: synchronized(lock) {
                buildDatabase(
                    context = context,
                    name = "easy_hiddify_app.db",
                ).also { INSTANCE = it }
            }
        }

        internal fun buildDatabase(
            context: Context,
            name: String,
        ): AppDB = Room.databaseBuilder(
            context = context,
            klass = AppDB::class.java,
            name = name,
        ).addMigrations(
            MIGRATION_1_2,
            MIGRATION_2_3,
            MIGRATION_3_4,
            MIGRATION_4_5,
            MIGRATION_5_6,
            MIGRATION_6_7,
            MIGRATION_7_8,
        ).build()
    }

    abstract fun appsDao(): AppsDao
    abstract fun serverDao(): ServerDao
}
