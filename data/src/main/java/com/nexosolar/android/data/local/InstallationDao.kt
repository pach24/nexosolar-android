package com.nexosolar.android.data.local


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface InstallationDao {

    // 1. Obtener la instalación (como Flow para actualizaciones reactivas)
    // Asumimos que el usuario suele tener UNA instalación principal, devolvemos List por si acaso
    @Query("SELECT * FROM installation")
    fun getInstallation(): Flow<List<InstallationEntity>>

    // 2. Insertar/Reemplazar instalación
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(installations: List<InstallationEntity>)

    // 3. Borrar todo (útil para pull-to-refresh)
    @Query("DELETE FROM installation")
    suspend fun deleteAll()

    // 4. Contar (para saber si la caché está vacía)
    @Query("SELECT COUNT(*) FROM installation")
    suspend fun getCount(): Int
}
