package com.example.apis3.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [FavoritoEntity::class, CacheEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoritoDao(): FavoritoDao
    abstract fun cacheDao(): CacheDao

    companion object {
        @Volatile
        private var instancia: AppDatabase? = null

        private val MIGRACION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `cache_busquedas` (" +
                            "`clave` TEXT NOT NULL, " +
                            "`titulo` TEXT NOT NULL, " +
                            "`subtitulo` TEXT NOT NULL, " +
                            "`imagen` TEXT, " +
                            "`datosJson` TEXT NOT NULL, " +
                            "PRIMARY KEY(`clave`))"
                )
            }
        }

        fun obtener(context: Context): AppDatabase =
            instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "apis3.db"
                )
                    .addMigrations(MIGRACION_1_2)
                    .build()
                    .also { instancia = it }
            }
    }
}