package com.easyhooon.samsunghealthexample.health.di

import android.content.Context
import com.samsung.android.sdk.health.data.HealthDataService
import com.samsung.android.sdk.health.data.HealthDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SamsungHealthModule {

    @Provides
    @Singleton
    fun provideHealthDataStore(@ApplicationContext context: Context): HealthDataStore {
        return HealthDataService.getStore(context)
    }
}
