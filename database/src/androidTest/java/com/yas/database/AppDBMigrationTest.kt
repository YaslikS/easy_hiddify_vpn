package com.yas.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDBMigrationTest {

    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun upgradesEveryReleasedSchemaAndPreservesSavedData() = runBlocking {
        for (version in 1..7) {
            val name = "migration-from-$version.db"
            context.deleteDatabase(name)
            try {
                createLegacyDatabase(
                    name = name,
                    version = version,
                )
                val db = AppDB.buildDatabase(
                    context = context,
                    name = name,
                )
                try {
                    val app = db.appsDao().getAll().single()
                    assertEquals("Version $version", "example.app", app.packageName)
                    assertFalse("Version $version", app.isSelected)
                    assertEquals("Version $version", 1, app.id)

                    val servers = db.serverDao().getAll()
                    assertEquals("Version $version", if (version >= 2) 1 else 0, servers.size)
                    servers.singleOrNull()?.let { server ->
                        assertEquals("test-config", server.config)
                        assertEquals(if (version == 7 || version == 6) 123L else -1L, server.pingMs)
                        assertEquals(if (version >= 4) "https://example.test/list" else null, server.subscriptionUrl)
                    }

                    val subscriptions = db.serverDao().subscriptions().first()
                    assertEquals("Version $version", if (version >= 4) 1 else 0, subscriptions.size)
                    subscriptions.singleOrNull()?.let { subscription ->
                        assertEquals(if (version >= 5) 123456L else null, subscription.expiresAt)
                        assertEquals(if (version >= 7) "Test subscription" else null, subscription.title)
                    }
                } finally {
                    db.close()
                }
            } finally {
                context.deleteDatabase(name)
            }
        }
    }

    private fun createLegacyDatabase(
        name: String,
        version: Int,
    ) {
        val path = context.getDatabasePath(name)
        path.parentFile?.mkdirs()
        SQLiteDatabase.openOrCreateDatabase(path, null).use { db ->
            db.execSQL(
                """CREATE TABLE app (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    packageName TEXT NOT NULL, name TEXT NOT NULL,
                    isSelected INTEGER NOT NULL, iconPath TEXT
                )""".trimIndent()
            )
            db.execSQL("INSERT INTO app VALUES (1, 'example.app', 'Example', 0, NULL)")
            db.execSQL("INSERT INTO app VALUES (2, 'example.app', 'Example duplicate', 1, NULL)")
            if (version >= 2) {
                db.execSQL(
                    "CREATE TABLE server (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "config TEXT NOT NULL, name TEXT NOT NULL)"
                )
                db.execSQL("INSERT INTO server VALUES (1, 'test-config', 'Test server')")
            }
            if (version >= 3) {
                db.execSQL("ALTER TABLE server ADD COLUMN pingMs INTEGER NOT NULL DEFAULT -1")
                db.execSQL("UPDATE server SET pingMs = 123")
            }
            if (version >= 4) {
                db.execSQL("ALTER TABLE server ADD COLUMN subscriptionUrl TEXT")
                db.execSQL("CREATE TABLE subscription (url TEXT NOT NULL PRIMARY KEY)")
                db.execSQL("INSERT INTO subscription VALUES ('https://example.test/list')")
                db.execSQL("UPDATE server SET subscriptionUrl = 'https://example.test/list'")
            }
            if (version >= 5) {
                db.execSQL("ALTER TABLE subscription ADD COLUMN expiresAt INTEGER")
                db.execSQL("UPDATE subscription SET expiresAt = 123456")
            }
            if (version >= 7) {
                db.execSQL("ALTER TABLE subscription ADD COLUMN title TEXT")
                db.execSQL("UPDATE subscription SET title = 'Test subscription'")
            }
            db.version = version
        }
    }
}
