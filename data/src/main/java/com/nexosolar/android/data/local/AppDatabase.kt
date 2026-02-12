package com.nexosolar.android.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * Base de datos local de la aplicación utilizando Room.
 *
 * Define la configuración principal de persistencia local para gestionar facturas.
 * Implementa el patrón Singleton thread-safe para garantizar una única instancia
 * en toda la aplicación, evitando overhead de memoria y problemas de concurrencia.
 *
 * Versión actual: 2 (con soporte para LocalDate mediante TypeConverters)
 */
@Database(
    entities = [InvoiceEntity::class, InstallationEntity::class],
    version = 4,
    exportSchema = false
)
@TypeConverters(RoomConverters::class)
abstract class AppDatabase : RoomDatabase() {

    /**
     * Proporciona acceso al DAO de facturas.
     *
     * @return Instancia del DAO para operaciones CRUD sobre facturas
     */
    abstract fun invoiceDao(): InvoiceDao
    abstract fun installationDao(): InstallationDao

    companion object {

        private const val DATABASE_NAME = "facturas_db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Obtiene la instancia única de la base de datos (Singleton thread-safe).
         *
         * Implementa Double-Checked Locking para optimizar el acceso concurrente.
         * Usa fallbackToDestructiveMigration() ya que la app está en fase de desarrollo
         * y no requiere migraciones complejas.
         *
         * @param context Contexto de la aplicación
         * @return Instancia única de AppDatabase
         */
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }

        /**
         * Construye la instancia de la base de datos con la configuración necesaria.
         *
         * @param context Contexto de la aplicación
         * @return Nueva instancia de AppDatabase
         */
        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                DATABASE_NAME
            )
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}
