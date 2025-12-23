package com.example.domain.repository

import kotlinx.coroutines.flow.Flow
import com.example.domain.entity.ListElementEntity

interface ListRepository {
    fun getElements(): Flow<List<ListElementEntity>>
    fun getElement(id: String): Flow<ListElementEntity?>
    suspend fun toggleLike(elementId: String)
}

