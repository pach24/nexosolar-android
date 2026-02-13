package com.nexosolar.android.data.source

import com.nexosolar.android.data.remote.InstallationDTO


interface InstallationRemoteDataSource {
    suspend fun getInstallation(): InstallationDTO
}
