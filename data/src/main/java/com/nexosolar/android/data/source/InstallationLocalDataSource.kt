package com.nexosolar.android.data.source

import com.nexosolar.android.data.local.InstallationEntity
import kotlinx.coroutines.flow.Flow

interface InstallationLocalDataSource {
    fun getInstallations(): Flow<List<InstallationEntity>>
    suspend fun replaceInstallations(installations: List<InstallationEntity>)
    suspend fun isCacheEmpty(): Boolean
    suspend fun deleteAll()
}
