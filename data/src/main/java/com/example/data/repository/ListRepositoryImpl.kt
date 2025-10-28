package com.example.data.repository

import com.example.data.mapper.toDomain
import com.example.data.network.ApiService
import com.example.domain.entity.ListElementEntity
import com.example.domain.repository.ListRepository

class ListRepositoryImpl(
    private val apiService: ApiService
) : ListRepository {
    override suspend fun getElements():
            Result<List<ListElementEntity>> {
        return try {
            val dtoList = apiService.getCatImages()
            Result.success(dtoList.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}