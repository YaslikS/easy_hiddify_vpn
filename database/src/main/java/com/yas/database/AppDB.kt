package com.yas.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.yas.database.dao.AppsDao
import com.yas.database.data.AppDTO


@Database(
    entities = [
        AppDTO::class,
    ],
    version = 1,
    exportSchema = false,
)
internal abstract class AppDB : RoomDatabase() {

    companion object {

        private val lock = Any()

        @Volatile
        private var INSTANCE: AppDB? = null

        /**
         * Получает базу данных
         */
        @Synchronized
        fun getDatabase(context: Context): AppDB {
            return INSTANCE ?: synchronized(lock) {
                Room.databaseBuilder(
                    context = context,
                    klass = AppDB::class.java,
                    name = "easy_hiddify_app.db",
                ).build().also {
                    INSTANCE = it
                }
            }
        }
    }

    abstract fun appsDao(): AppsDao
}