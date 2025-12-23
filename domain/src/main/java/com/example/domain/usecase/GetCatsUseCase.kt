package com.example.domain.usecase

import kotlinx.coroutines.flow.Flow
import com.example.domain.entity.ListElementEntity
import com.example.domain.repository.ListRepository

class GetCatsUseCase(
    private val listRepository: ListRepository
) : UseCase<Unit, Flow<List<ListElementEntity>>> {
    
    override fun execute(data: Unit): Flow<List<ListElementEntity>> {
        return listRepository.getElements()
    }
}

