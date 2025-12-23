package com.example.domain.usecase

import kotlinx.coroutines.flow.Flow
import com.example.domain.entity.ListElementEntity
import com.example.domain.repository.ListRepository

/**
 * Use Case для получения одного котика по ID.
 * Принимает String (ID элемента), возвращает Flow с ListElementEntity или null.
 */
class GetCatsByIdUseCase(
    private val listRepository: ListRepository
) : UseCase<String, Flow<ListElementEntity?>> {
    
    override fun execute(data: String): Flow<ListElementEntity?> {
        // UseCase просто делегирует вызов репозиторию.
        // Здесь может быть дополнительная бизнес-логика (сортировка, фильтрация),
        // но НЕ маппинг.
        return listRepository.getElement(data)
    }
}

