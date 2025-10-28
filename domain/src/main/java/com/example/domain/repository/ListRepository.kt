package com.example.domain.repository

import com.example.domain.entity.ListElementEntity

interface ListRepository {
    suspend fun getElements(): Result<List<ListElementEntity>>
    suspend fun getElementById(id: String): Result<ListElementEntity>
}