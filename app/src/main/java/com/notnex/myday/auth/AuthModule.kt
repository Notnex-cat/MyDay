package com.notnex.myday.auth

import android.content.Context
import com.google.android.gms.auth.api.identity.Identity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Provides
    @Singleton
    fun provideAuthRepository(
        @ApplicationContext context: Context
    ): AuthRepository {
        val client = Identity.getSignInClient(context)
        return AuthRepository(client)
    }
}
