package com.nexosolar.android.data.source

import com.nexosolar.android.data.remote.ApiService
import com.nexosolar.android.data.remote.InstallationDTO

class InstallationRemoteDataSourceImpl(
    private val apiService: ApiService
) : InstallationRemoteDataSource {

    override suspend fun getInstallation(): InstallationDTO {
        return apiService.getInstallationDetails()
    }
}
