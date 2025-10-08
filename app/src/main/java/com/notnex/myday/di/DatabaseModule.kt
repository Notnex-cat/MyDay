package com.notnex.myday.di

import android.content.Context
import androidx.room.Room
import com.notnex.myday.data.MyDayDAO
import com.notnex.myday.data.MyDayDataBase
import com.notnex.myday.data.MyScheduleDAO
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext appContext: Context
    ): MyDayDataBase {
        return Room.databaseBuilder(
            appContext,
            MyDayDataBase::class.java,
            "day_entries.db"
        ).build()
    }

    @Provides
    fun provideMyDayDao(db: MyDayDataBase): MyDayDAO = db.myDayDao()

    @Provides
    fun provideScheduleDao(db: MyDayDataBase): MyScheduleDAO = db.myScheduleDao()
}