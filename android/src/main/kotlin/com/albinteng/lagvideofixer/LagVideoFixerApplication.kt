package com.albinteng.lagvideofixer

import android.app.Application
import com.albinteng.lagvideofixer.data.backend.androidBackends
import com.albinteng.lagvideofixer.data.output.AndroidOutputStore
import com.albinteng.lagvideofixer.data.queue.WorkKeeper
import com.albinteng.lagvideofixer.data.settings.DataStoreSettingsRepository
import com.albinteng.lagvideofixer.domain.port.OutputStore
import com.albinteng.lagvideofixer.domain.port.SettingsRepository
import com.albinteng.lagvideofixer.domain.port.VideoBackends
import com.albinteng.lagvideofixer.presentation.di.sharedModule
import com.albinteng.lagvideofixer.service.ServiceWorkKeeper
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module
import java.io.File

class LagVideoFixerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@LagVideoFixerApplication)
            modules(sharedModule, androidModule)
        }
    }
}

/** The Android implementations of what the shared module needs. */
private val androidModule = module {
    // the same file the former preferencesDataStore("settings") used, so saved settings survive
    single<SettingsRepository> { DataStoreSettingsRepository(File(androidContext().filesDir, "datastore/settings.preferences_pb").path) }
    single<OutputStore> { AndroidOutputStore(androidContext(), get()) }
    single<VideoBackends> { androidBackends(androidContext()) }
    single<WorkKeeper> { ServiceWorkKeeper(androidContext()) }
}
