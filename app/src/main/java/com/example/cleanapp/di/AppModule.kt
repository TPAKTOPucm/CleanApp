package com.example.cleanapp.di

import androidx.lifecycle.SavedStateHandle
import com.example.cleanapp.details.vm.DetailsViewModel
import com.example.cleanapp.main.vm.MainViewModel
import com.example.data.network.ApiService
import com.example.data.repository.ListRepositoryImpl
import com.example.domain.repository.ListRepository
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit

val appModule = module {
    viewModel { MainViewModel(listRepository = get()) }
// ДОБАВЛЕНО: Регистрация ViewModel для экрана деталей
    viewModel { params -> // Используем params для передачи
        SavedStateHandle
        DetailsViewModel(
            savedStateHandle = params.get(),
            repository = get()
        )
    }
// Network
    single<ApiService> {
        val contentType = "application/json".toMediaType()
        val json = Json { ignoreUnknownKeys = true }
        Retrofit.Builder()
            .baseUrl("https://api.thecatapi.com/")
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(ApiService::class.java)
    }
// Repository
    single<ListRepository> { ListRepositoryImpl(apiService = get()) }
}