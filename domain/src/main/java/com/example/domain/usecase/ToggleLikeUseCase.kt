package com.example.domain.usecase

import com.example.domain.repository.ListRepository

class ToggleLikeUseCase(
    private val listRepository: ListRepository
) : UseCase<String, Unit> {
    
    override fun execute(data: String) {
        throw UnsupportedOperationException("Use executeSuspend instead")
    }

    suspend fun executeSuspend(elementId: String) {
        listRepository.toggleLike(elementId)
    }
}

