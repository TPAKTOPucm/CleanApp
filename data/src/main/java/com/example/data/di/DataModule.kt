package com.example.data.di

import androidx.room.Room
import coil.ImageLoader
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import retrofit2.Retrofit
import com.example.data.database.AppDatabase
import com.example.data.database.CatCacheEntity
import com.example.data.mapper.CatCacheToDomainMapper
import com.example.data.mapper.CatDtoToCacheMapper
import com.example.data.network.ApiService
import com.example.data.network.response.CatImageDto
import com.example.data.repository.ListRepositoryImpl
import com.example.domain.entity.ListElementEntity
import com.example.domain.mapper.Mapper
import com.example.domain.repository.ListRepository
import com.example.data.di.DiQualifiers.DTO_TO_CACHE_MAPPER
import com.example.data.di.DiQualifiers.CACHE_TO_DOMAIN_MAPPER

val dataModule = module {
    single<ApiService> {
        val contentType = "application/json".toMediaType()
        val json = Json { ignoreUnknownKeys = true }
        Retrofit.Builder()
            .baseUrl("https://api.thecatapi.com/")
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(ApiService::class.java)
    }

    single { 
        Room.databaseBuilder(
            androidContext(), 
            AppDatabase::class.java, 
            "app_database"
        ).build() 
    }
    single { get<AppDatabase>().catDao() }

    single {
        ImageLoader.Builder(androidContext())
            .respectCacheHeaders(false) // Важно для некоторых API
            .build()
    }

    factory<Mapper<CatImageDto, CatCacheEntity>>(DTO_TO_CACHE_MAPPER) { 
        CatDtoToCacheMapper() 
    }
    factory<Mapper<CatCacheEntity, ListElementEntity>>(CACHE_TO_DOMAIN_MAPPER) { 
        CatCacheToDomainMapper() 
    }

    single<ListRepository> { 
        ListRepositoryImpl(
            get(), 
            get(),
            dtoToCacheMapper = get(qualifier = DTO_TO_CACHE_MAPPER),
            cacheToDomainMapper = get(qualifier = CACHE_TO_DOMAIN_MAPPER),
            get(),
            androidContext()
        ) 
    }
}

