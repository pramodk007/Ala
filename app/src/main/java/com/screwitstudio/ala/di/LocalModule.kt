package com.screwitstudio.ala.di

import android.content.Context
import androidx.room.Room
import com.screwitstudio.ala.data.local.AlarmDao
import com.screwitstudio.ala.data.local.AlarmDatabase
import com.screwitstudio.ala.repository.AlarmRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocalModule {

    @Provides
    @Singleton
    fun provideAlarmDatabase(@ApplicationContext context: Context): AlarmDatabase {
        return Room.databaseBuilder(context, AlarmDatabase::class.java, "AlarmDatabase")
            .addMigrations(AlarmDatabase.MIGRATION_1_2,AlarmDatabase.MIGRATION_2_3)
            .build()
    }

    @Provides
    @Singleton
    fun provideAlarmDao(database: AlarmDatabase): AlarmDao {
        return database.alarmDao()
    }

    @Provides
    fun provideAlarmRepo(alarmDao: AlarmDao): AlarmRepository = AlarmRepository(alarmDao)

}