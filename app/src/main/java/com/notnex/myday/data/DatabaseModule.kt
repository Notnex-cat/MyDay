package com.notnex.myday.data

import android.content.Context
import androidx.room.Room
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
    fun provideDatabase(@ApplicationContext context: Context): MyDayDataBase {
        return Room.databaseBuilder(
            context,
            MyDayDataBase::class.java,
            "my_day.db"
        ).build()
    }

    @Provides
    fun provideMyDayDao(database: MyDayDataBase): MyDayDAO {
        return database.myDayDao()
    }
}