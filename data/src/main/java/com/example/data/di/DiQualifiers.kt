package com.example.data.di

import org.koin.core.qualifier.named

object DiQualifiers {
    // Уникальное имя для маппера DTO -> Cache
    val DTO_TO_CACHE_MAPPER = named("DtoToCacheMapper")
    // Уникальное имя для маппера Cache -> Domain
    val CACHE_TO_DOMAIN_MAPPER = named("CacheToDomainMapper")
}
