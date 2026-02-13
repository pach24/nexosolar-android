package com.nexosolar.android.di

import com.nexosolar.android.data.repository.*
import com.nexosolar.android.data.source.*
import com.nexosolar.android.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    // --- REPOSITORIOS (Domain <-> Data) ---

    @Binds
    @Singleton
    abstract fun bindInvoiceRepository(
        impl: InvoiceRepositoryImpl
    ): InvoiceRepository

    @Binds
    @Singleton
    abstract fun bindInstallationRepository(
        impl: InstallationRepositoryImpl
    ): InstallationRepository

    // --- DATA SOURCES (Data Layer Internals) ---

    // Remote Sources
    @Binds
    abstract fun bindInvoiceRemoteDataSource(
        impl: InvoiceRemoteDataSourceImpl
    ): InvoiceRemoteDataSource

    @Binds
    abstract fun bindInstallationRemoteDataSource(
        impl: InstallationRemoteDataSourceImpl
    ): InstallationRemoteDataSource

    // Local Sources
    @Binds
    abstract fun bindInvoiceLocalDataSource(
        impl: InvoiceLocalDataSourceImpl
    ): InvoiceLocalDataSource

    @Binds
    abstract fun bindInstallationLocalDataSource(
        impl: InstallationLocalDataSourceImpl
    ): InstallationLocalDataSource
}
