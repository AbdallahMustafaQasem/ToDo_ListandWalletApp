package com.abdallah.taskvault.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

// UserPreferencesRepository is @Singleton with @Inject constructor — Hilt finds it automatically.
// This module exists only if extra bindings are needed in future.
@Module
@InstallIn(SingletonComponent::class)
object PreferencesModule
