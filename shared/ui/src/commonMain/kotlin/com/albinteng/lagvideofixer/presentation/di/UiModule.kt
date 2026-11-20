package com.albinteng.lagvideofixer.presentation.di

import com.albinteng.lagvideofixer.data.queue.ProcessingQueueImpl
import com.albinteng.lagvideofixer.domain.port.ProcessingQueue
import com.albinteng.lagvideofixer.domain.usecase.FixVideoUseCase
import com.albinteng.lagvideofixer.presentation.main.MainViewModel
import com.albinteng.lagvideofixer.presentation.settings.SettingsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Everything that is the same on both platforms. Each app adds: SettingsRepository, OutputStore, VideoBackends
 * and WorkKeeper.
 */
val sharedModule = module {
    single { FixVideoUseCase(get(), get()) }
    single<ProcessingQueue> { ProcessingQueueImpl(get(), get()) }
    viewModel { MainViewModel(get(), get()) }
    viewModel { SettingsViewModel(get(), get()) }
}
