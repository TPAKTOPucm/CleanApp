package com.example.cleanapp.di

import com.example.cleanapp.main.vm.MainViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    viewModel { MainViewModel() }
}