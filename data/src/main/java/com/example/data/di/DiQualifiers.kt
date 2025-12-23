package com.example.data.di

import org.koin.core.qualifier.named

object DiQualifiers {
    val DTO_TO_CACHE_MAPPER = named("DtoToCacheMapper")
    val CACHE_TO_DOMAIN_MAPPER = named("CacheToDomainMapper")
}

