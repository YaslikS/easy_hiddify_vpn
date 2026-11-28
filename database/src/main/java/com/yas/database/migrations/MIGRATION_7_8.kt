package com.yas.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Preserve an explicit VPN exclusion when duplicate rows disagree.
        db.execSQL(
            """
            DELETE FROM app
            WHERE id NOT IN (
                SELECT COALESCE(MAX(CASE WHEN isSelected = 0 THEN id END), MAX(id))
                FROM app
                GROUP BY packageName
            )
            """.trimIndent()
        )
        db.execSQL("CREATE UNIQUE INDEX index_app_packageName ON app (packageName)")
    }
}
