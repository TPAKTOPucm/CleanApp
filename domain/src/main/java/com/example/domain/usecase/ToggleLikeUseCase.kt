package com.example.domain.usecase

import com.example.domain.repository.ListRepository

/**
 * Use Case для переключения статуса лайкнуто у элемента
 */
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

