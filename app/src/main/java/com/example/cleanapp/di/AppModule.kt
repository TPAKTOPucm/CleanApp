package com.example.cleanapp.di

import androidx.work.WorkManager
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.androidx.workmanager.dsl.worker
import org.koin.dsl.module
import com.example.cleanapp.details.vm.DetailsViewModel
import com.example.cleanapp.main.vm.MainViewModel
import com.example.cleanapp.workers.FilterWorker
import com.example.domain.usecase.GetCatsUseCase
import com.example.domain.usecase.GetCatsByIdUseCase

val appModule = module {
    // Use Cases
    factory { GetCatsUseCase(get()) }
    factory { GetCatsByIdUseCase(get()) }

    // WorkManager (остаётся - это 8-я практика)
    single { WorkManager.getInstance(androidContext()) }

    // Workers (остаётся - это 8-я практика)
    worker { FilterWorker(get(), get(), get()) }

    // ViewModels
    viewModel { MainViewModel(get()) }

    viewModel { params ->
        DetailsViewModel(
            getCatsByIdUseCase = get<GetCatsByIdUseCase>(),
            workManager = get(),
            savedStateHandle = params.get()
        )
    }
}