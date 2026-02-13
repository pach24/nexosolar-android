package com.nexosolar.android.data.source

import com.nexosolar.android.data.remote.ApiService
import com.nexosolar.android.data.remote.InstallationDTO
import javax.inject.Inject

class InstallationRemoteDataSourceImpl @Inject constructor(
    private val apiService: ApiService
) : InstallationRemoteDataSource {

    override suspend fun getInstallation(): InstallationDTO {
        return apiService.getInstallationDetails()
    }
}
