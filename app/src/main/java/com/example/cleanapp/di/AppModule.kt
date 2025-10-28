package com.example.cleanapp.di

import androidx.lifecycle.SavedStateHandle
import com.example.cleanapp.details.vm.DetailsViewModel
import com.example.cleanapp.main.vm.MainViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    viewModel { MainViewModel() }
    viewModel { params ->
        SavedStateHandle
        DetailsViewModel(
            savedStateHandle = params.get()
        )
    }
}