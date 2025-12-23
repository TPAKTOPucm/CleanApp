package com.example.domain.usecase

import kotlinx.coroutines.flow.Flow
import com.example.domain.entity.ListElementEntity
import com.example.domain.repository.ListRepository

class GetCatsByIdUseCase(
    private val listRepository: ListRepository
) : UseCase<String, Flow<ListElementEntity?>> {
    
    override fun execute(data: String): Flow<ListElementEntity?> {
        return listRepository.getElement(data)
    }
}

