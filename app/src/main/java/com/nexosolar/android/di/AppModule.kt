package com.nexosolar.android.di

import android.content.Context
import com.nexosolar.android.data.local.AppDatabase
import com.nexosolar.android.data.local.InstallationDao
import com.nexosolar.android.data.local.InvoiceDao
import com.nexosolar.android.data.remote.ApiClientManager
import com.nexosolar.android.data.remote.ApiService
import com.nexosolar.android.data.remote.DynamicApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // --- ROOM ---

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getInstance(context)
    }

    @Provides
    fun provideInvoiceDao(db: AppDatabase): InvoiceDao {
        return db.invoiceDao()
    }


    @Provides
    fun provideInstallationDao(db: AppDatabase): InstallationDao {
        return db.installationDao()
    }

    // --- RED (RETROFIT / API) ---

    @Provides
    @Singleton
    fun provideApiService(dynamicApiService: DynamicApiService): ApiService {
        return dynamicApiService
    }
}
