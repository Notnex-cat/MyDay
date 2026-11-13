package com.notnex.myday.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

// Один файл настроек на всё приложение, общий для всех репозиториев настроек. Свойство объявлено
// вне классов, чтобы DataStore создавался один раз: два DataStore на один файл — это падение.
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
