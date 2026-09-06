package com.yas.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `server` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `config` TEXT NOT NULL,
                `name` TEXT NOT NULL
            )
            """.trimIndent()
        )
    }
}