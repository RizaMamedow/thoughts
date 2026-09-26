package dev.thoughts.app.di

import dev.thoughts.app.data.AppDatabase
import dev.thoughts.app.data.settings.SettingsRepository
import dev.thoughts.app.ui.features.thoughts.ThoughtsScreenModel
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {
    single { AppDatabase.get(androidContext()) }
    single { get<AppDatabase>().thoughtDao() }
    single { SettingsRepository(androidContext()) }
    factory { ThoughtsScreenModel(get(), get()) }
}
