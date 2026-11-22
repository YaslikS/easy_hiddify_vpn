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


@Database(
    entities = [
        AppDTO::class,
        ServerDTO::class,
        SubscriptionDTO::class,
    ],
    version = 7,
    exportSchema = false,
)
internal abstract class AppDB : RoomDatabase() {

    companion object {

        private val lock = Any()

        @Volatile
        private var INSTANCE: AppDB? = null

        @Synchronized
        fun getDatabase(context: Context): AppDB {
            return INSTANCE ?: synchronized(lock) {
                Room.databaseBuilder(
                    context = context,
                    klass = AppDB::class.java,
                    name = "easy_hiddify_app.db",
                )
                    .addMigrations(MIGRATION_1_2)
                    .addMigrations(MIGRATION_2_3)
                    .addMigrations(MIGRATION_3_4)
                    .addMigrations(MIGRATION_4_5)
                    .addMigrations(MIGRATION_5_6)
                    .addMigrations(MIGRATION_6_7)
                    .build().also {
                        INSTANCE = it
                    }
            }
        }
    }

    abstract fun appsDao(): AppsDao
    abstract fun serverDao(): ServerDao
}
