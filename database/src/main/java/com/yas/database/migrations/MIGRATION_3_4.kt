package com.yas.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE server ADD COLUMN subscriptionUrl TEXT")
        db.execSQL("CREATE TABLE IF NOT EXISTS subscription (url TEXT NOT NULL PRIMARY KEY)")
    }
}
