package com.example.domain.repository

import com.example.domain.entity.ListElementEntity
import kotlinx.coroutines.flow.Flow

interface ListRepository {
    fun getElements(): Flow<List<ListElementEntity>>
    fun getElement(id: String): Flow<ListElementEntity?>
}

