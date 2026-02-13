package com.nexosolar.android.data.source

import com.nexosolar.android.data.local.InstallationDao
import com.nexosolar.android.data.local.InstallationEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class InstallationLocalDataSourceImpl @Inject constructor(
    private val dao: InstallationDao
) : InstallationLocalDataSource {

    override fun getInstallations(): Flow<List<InstallationEntity>> {
        return dao.getInstallation()
    }

    override suspend fun replaceInstallations(installations: List<InstallationEntity>) {
        dao.deleteAll()
        dao.insertAll(installations)
    }

    override suspend fun isCacheEmpty(): Boolean {
        return dao.getCount() == 0
    }

    override suspend fun deleteAll() {
        dao.deleteAll()
    }
}
