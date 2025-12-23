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
import com.example.domain.usecase.ToggleLikeUseCase

val appModule = module {
    // Use Cases
    factory { GetCatsUseCase(get()) }
    factory { GetCatsByIdUseCase(get()) }
    factory { ToggleLikeUseCase(get()) }

    // WorkManager
    single { WorkManager.getInstance(androidContext()) }

    // Workers
    worker { FilterWorker(get(), get(), get()) }

    // ViewModels
    viewModel { MainViewModel(get(), get()) }

    viewModel { params ->
        DetailsViewModel(
            getCatsByIdUseCase = get<GetCatsByIdUseCase>(),
            workManager = get(),
            savedStateHandle = params.get()
        )
    }
}
